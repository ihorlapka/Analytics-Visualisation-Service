package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TelemetryEvent;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface TelemetryRepository {

    <T extends TelemetryEvent> Flux<T> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant start, Instant end);
}
