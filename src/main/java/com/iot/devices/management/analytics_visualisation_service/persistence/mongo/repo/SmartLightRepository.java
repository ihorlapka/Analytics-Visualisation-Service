package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartLightEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface SmartLightRepository extends ReactiveMongoRepository<SmartLightEvent, UUID> {

    Mono<SmartLightEvent> findByDeviceIdAndLastUpdated(UUID deviceId, Instant lastUpdated);
}
