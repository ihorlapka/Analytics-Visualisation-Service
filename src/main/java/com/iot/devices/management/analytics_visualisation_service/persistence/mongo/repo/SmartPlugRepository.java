package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface SmartPlugRepository extends ReactiveMongoRepository<SmartPlugEvent, UUID> {

    Mono<SmartPlugEvent> findByDeviceIdAndLastUpdated(UUID deviceId, Instant lastUpdated);
}
