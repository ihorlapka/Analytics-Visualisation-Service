package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.EnergyMeterEvent;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface EnergyMeterRepository extends TelemetryRepository<EnergyMeterEvent> {

    Flux<EnergyMeterEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);
}
