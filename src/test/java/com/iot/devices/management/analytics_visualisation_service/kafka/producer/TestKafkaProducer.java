package com.iot.devices.management.analytics_visualisation_service.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderResult;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
public class TestKafkaProducer {

    private final KafkaProducerProperties producerProperties;
    private final ReactiveKafkaProducerTemplate<String, SpecificRecord> reactiveKafkaProducerTemplate;


    public TestKafkaProducer(KafkaProducerProperties producerProperties) {
        Properties properties = new Properties();
        properties.putAll(producerProperties.getProperties());
        this.producerProperties = producerProperties;
        this.reactiveKafkaProducerTemplate = new ReactiveKafkaProducerTemplate<>(SenderOptions.create(properties));
    }

    public Mono<SenderResult<Void>> sendMessage(SpecificRecord record, UUID key) {
        try {
            return reactiveKafkaProducerTemplate.send(producerProperties.getTopic(), key.toString(), record)
                    .doOnSuccess(result -> log.info("Message is successfully sent {}", record))
                    .doOnError(error -> System.err.println("Failed to send message: " + error.getMessage()));
        } catch (Exception e) {
            log.error("Failed to send message: {}", record, e);
            throw new RuntimeException("Unable to send message!");
        }
    }
}
