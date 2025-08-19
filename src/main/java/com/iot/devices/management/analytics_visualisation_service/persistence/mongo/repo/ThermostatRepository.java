package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.ThermostatEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ThermostatRepository extends ReactiveMongoRepository<ThermostatEvent, UUID> {

    Optional<ThermostatEvent> findByDeviceIdAndLastUpdated(UUID deviceId, Instant lastUpdated);
}
