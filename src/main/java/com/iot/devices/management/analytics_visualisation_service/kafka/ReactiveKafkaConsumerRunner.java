package com.iot.devices.management.analytics_visualisation_service.kafka;

import com.iot.alerts.Alert;
import com.iot.devices.management.analytics_visualisation_service.stream.AlertStream;
import com.iot.devices.management.analytics_visualisation_service.stream.TelemetryStream;
import io.micrometer.core.instrument.MeterRegistry;
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
import reactor.kafka.receiver.MicrometerConsumerListener;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Long.MAX_VALUE;
import static java.time.Duration.ofSeconds;

@Slf4j
@Component
public class ReactiveKafkaConsumerRunner {

    private final TelemetryStream telemetryStream;
    private final AlertStream alertStream;
    private final KafkaConsumerProperties consumerProperties;
    private final AtomicBoolean kafkaConsumerStatusMonitor;
    private final ReactiveKafkaConsumerTemplate<String, SpecificRecord> consumerTemplate;

    private Disposable subscription;

    public ReactiveKafkaConsumerRunner(KafkaConsumerProperties consumerProperties, TelemetryStream telemetryStream, AlertStream alertStream,
                                       AtomicBoolean kafkaConsumerStatusMonitor, MeterRegistry meterRegistry) {
        this.consumerProperties = consumerProperties;
        this.telemetryStream = telemetryStream;
        this.alertStream = alertStream;
        this.kafkaConsumerStatusMonitor = kafkaConsumerStatusMonitor;
        this.consumerTemplate = new ReactiveKafkaConsumerTemplate<>(createReceiverOptions(consumerProperties, meterRegistry));
    }

    private ReceiverOptions<String, SpecificRecord> createReceiverOptions(KafkaConsumerProperties consumerProperties,
                                                                          MeterRegistry meterRegistry) {
        final Properties properties = new Properties();
        properties.putAll(consumerProperties.getProperties());
        final ReceiverOptions<String, SpecificRecord> receiverOptions = ReceiverOptions.create(properties);
        return receiverOptions.subscription(Collections.singletonList(consumerProperties.getTopic()))
                .addAssignListener(partitions -> {
                    log.info("onPartitionsAssigned : {}", partitions);
                    kafkaConsumerStatusMonitor.set(true);
                })
                .addRevokeListener(partitions -> {
                    log.info("onPartitionsRevoked : {}", partitions);
                    kafkaConsumerStatusMonitor.set(false);
                })
                .consumerListener(new MicrometerConsumerListener(meterRegistry));
    }

    @PostConstruct
    public void startConsumer() {
        subscription = consumerTemplate.receive()
                .limitRate(consumerProperties.getPollLimitRate())
                .flatMap(record -> {
                    if (record.value() instanceof Alert alert) {
                        return alertStream.publish(alert)
                                .doOnSuccess(ignored -> logAndAcknowledge(record))
                                .onErrorResume(error -> logAndAcknowledgeIfNonRetriableError(record, error));
                    } else {
                        return telemetryStream.publish(record.value())
                                .doOnSuccess(ignored -> logAndAcknowledge(record))
                                .onErrorResume(error -> logAndAcknowledgeIfNonRetriableError(record, error));
                    }
                })
                .doOnError(error -> log.error("Consumer error: {}", error.getMessage()))
                .retryWhen(Retry.backoff(MAX_VALUE, ofSeconds(consumerProperties.getBackoffTimeSeconds()))
                        .maxBackoff(ofSeconds(consumerProperties.getMaxBackoffTimeSeconds())))
                .subscribe();
    }

    private void logAndAcknowledge(ReceiverRecord<String, SpecificRecord> record) {
        final ReceiverOffset offset = record.receiverOffset();
        log.info("Received message: {}, offset: {}", record.value(), offset.offset());
        offset.acknowledge();
    }

    private Mono<Void> logAndAcknowledgeIfNonRetriableError(ReceiverRecord<String, SpecificRecord> record, Throwable error) {
        if (isNonRetriable(error)) {
            log.error("Non-retriable error, skipping message: {}, record: {}", error.getMessage(), record.value());
            record.receiverOffset().acknowledge();
            return Mono.empty();
        } else {
            return Mono.error(error);
        }
    }

    private boolean isNonRetriable(Throwable error) {
        return error instanceof IllegalArgumentException
                || error instanceof IllegalStateException
                || error instanceof DeserializationException
                || error instanceof ListenerExecutionFailedException;
    }

    @PreDestroy
    public void shutdown() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            kafkaConsumerStatusMonitor.set(false);
            log.info("Kafka consumer subscription disposed gracefully.");
        }
    }
}
