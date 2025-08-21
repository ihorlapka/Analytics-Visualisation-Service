package com.iot.devices.management.analytics_visualisation_service.kafka;

import com.iot.devices.management.analytics_visualisation_service.stream.TelemetryStream;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.util.Collections;
import java.util.Properties;

import static java.lang.Long.MAX_VALUE;
import static java.time.Duration.ofSeconds;

@Slf4j
@Component
public class ReactiveKafkaConsumerRunner {

    private final TelemetryStream telemetryStream;
    private final KafkaConsumerProperties consumerProperties;
    private final ReactiveKafkaConsumerTemplate<String, SpecificRecord> consumerTemplate;

    private Disposable subscription;

    public ReactiveKafkaConsumerRunner(KafkaConsumerProperties consumerProperties, TelemetryStream telemetryStream) {
        this.consumerProperties = consumerProperties;
        this.telemetryStream = telemetryStream;
        this.consumerTemplate = new ReactiveKafkaConsumerTemplate<>(createReceiverOptions(consumerProperties));
    }

    private ReceiverOptions<String, SpecificRecord> createReceiverOptions(KafkaConsumerProperties consumerProperties) {
        final Properties properties = new Properties();
        properties.putAll(consumerProperties.getProperties());
        final ReceiverOptions<String, SpecificRecord> receiverOptions = ReceiverOptions.create(properties);
        receiverOptions.subscription(Collections.singletonList(consumerProperties.getTopic()));
        return receiverOptions;
    }

    @PostConstruct
    public void consumeRecord() {
        subscription = consumerTemplate.receive()
                .limitRate(consumerProperties.getPollLimitRate())
                .flatMap(record -> telemetryStream.publish(record.value())
                        .doOnSuccess(ignored -> record.receiverOffset().acknowledge())
                        .onErrorResume(error -> logAndSkipIfNonRetriableError(record, error)))
                .doOnError(error -> log.error("Consumer error: {}", error.getMessage()))
                .retryWhen(Retry.backoff(MAX_VALUE, ofSeconds(consumerProperties.getBackoffTimeSeconds()))
                        .maxBackoff(ofSeconds(consumerProperties.getMaxBackoffTimeSeconds())))
                .subscribe();
    }

    private Mono<Void> logAndSkipIfNonRetriableError(ReceiverRecord<String, SpecificRecord> record, Throwable error) {
        if (isNonRetriable(error)) {
            log.error("Non-retriable error, skipping message: {}", error.getMessage());
            record.receiverOffset().acknowledge();
            return Mono.empty();
        } else {
            return Mono.error(error);
        }
    }

    public boolean isNonRetriable(Throwable error) {
        return error instanceof NullPointerException
                || error instanceof IllegalArgumentException
                || error instanceof IllegalStateException
                || error instanceof DeserializationException
                || error instanceof ListenerExecutionFailedException;
    }

    @PreDestroy
    public void shutdown() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Kafka consumer subscription disposed gracefully.");
        }
    }
}
