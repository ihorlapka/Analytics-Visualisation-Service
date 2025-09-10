package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.DoorSensorEvent;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface DoorSensorRepository extends TelemetryRepository<DoorSensorEvent> {

    Flux<DoorSensorEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);
}
