package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.ThermostatEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface ThermostatRepository extends TelemetryRepository<ThermostatEvent> {

    Flux<ThermostatEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<ThermostatEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
