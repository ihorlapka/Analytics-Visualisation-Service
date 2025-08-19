package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.EnergyMeterEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface EnergyMeterRepository extends ReactiveMongoRepository<EnergyMeterEvent, UUID> {

    Mono<EnergyMeterEvent> findByDeviceIdAndLastUpdated(UUID deviceId, Instant lastUpdated);
}
