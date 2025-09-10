package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TemperatureSensorEvent;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface TemperatureSensorRepository extends TelemetryRepository<TemperatureSensorEvent> {

    Flux<TemperatureSensorEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);
}
