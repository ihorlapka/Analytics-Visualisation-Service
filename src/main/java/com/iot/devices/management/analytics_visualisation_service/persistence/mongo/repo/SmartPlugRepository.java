package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface SmartPlugRepository extends TelemetryRepository<SmartPlugEvent> {

    Flux<SmartPlugEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);
}
