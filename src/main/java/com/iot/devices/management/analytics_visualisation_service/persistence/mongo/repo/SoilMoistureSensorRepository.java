package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SoilMoistureSensorEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface SoilMoistureSensorRepository extends TelemetryRepository<SoilMoistureSensorEvent> {

    Flux<SoilMoistureSensorEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<SoilMoistureSensorEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
