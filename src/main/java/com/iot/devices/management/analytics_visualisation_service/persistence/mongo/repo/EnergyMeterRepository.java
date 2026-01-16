package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.EnergyMeterEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.ENERGY_METER;

public interface EnergyMeterRepository extends TelemetryRepository<EnergyMeterEvent> {

    @Override
    default DeviceType getDeviceType() {
        return ENERGY_METER;
    }

    Flux<EnergyMeterEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<EnergyMeterEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
