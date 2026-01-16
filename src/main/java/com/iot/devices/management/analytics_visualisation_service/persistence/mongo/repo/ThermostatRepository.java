package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.ThermostatEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.THERMOSTAT;

public interface ThermostatRepository extends TelemetryRepository<ThermostatEvent> {

    @Override
    default DeviceType getDeviceType() {
        return THERMOSTAT;
    }

    Flux<ThermostatEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<ThermostatEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
