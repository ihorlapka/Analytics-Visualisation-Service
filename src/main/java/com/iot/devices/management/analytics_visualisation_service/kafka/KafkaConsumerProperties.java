package com.iot.devices.management.analytics_visualisation_service.kafka;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(KafkaConsumerProperties.PROPERTIES_PREFIX)
@RequiredArgsConstructor
public class KafkaConsumerProperties {

    final static String PROPERTIES_PREFIX = "kafka.consumer";

    private Map<String, String> properties = new HashMap<>();

    @Value("${" + PROPERTIES_PREFIX + ".topic}")
    private String topic;

    @Value("${" + PROPERTIES_PREFIX + ".poll-limit-rate}")
    private int pollLimitRate;

    @Value("${" + PROPERTIES_PREFIX + ".backoff-time-seconds}")
    private Long backoffTimeSeconds;

    @Value("${" + PROPERTIES_PREFIX + ".max-backoff-time-seconds}")
    private Long maxBackoffTimeSeconds;

    @PostConstruct
    private void logProperties() {
        log.info("kafka consumer properties: {}", this);
    }
}
