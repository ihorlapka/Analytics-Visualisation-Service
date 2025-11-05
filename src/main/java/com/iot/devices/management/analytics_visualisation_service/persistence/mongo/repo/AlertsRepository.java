package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.AlertEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface AlertsRepository  extends ReactiveMongoRepository<AlertEvent, UUID> {

    Flux<AlertEvent> findByDeviceIdAndTimestampBetween(UUID deviceId, Instant start, Instant end);

    Mono<AlertEvent> findFirstByDeviceIdOrderByTimestampDesc(UUID deviceId);
}
