package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TemperatureSensorEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TemperatureSensorRepository extends ReactiveMongoRepository<TemperatureSensorEvent, UUID> {

    Optional<TemperatureSensorEvent> findByDeviceIdAndLastUpdated(UUID deviceId, Instant lastUpdated);
}
