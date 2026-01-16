package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TemperatureSensorEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.TEMPERATURE_SENSOR;

public interface TemperatureSensorRepository extends TelemetryRepository<TemperatureSensorEvent> {

    @Override
    default DeviceType getDeviceType() {
        return TEMPERATURE_SENSOR;
    }

    Flux<TemperatureSensorEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<TemperatureSensorEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
