package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SoilMoistureSensorEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.SOIL_MOISTURE_SENSOR;

public interface SoilMoistureSensorRepository extends TelemetryRepository<SoilMoistureSensorEvent> {

    @Override
    default DeviceType getDeviceType() {
        return SOIL_MOISTURE_SENSOR;
    }

    Flux<SoilMoistureSensorEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<SoilMoistureSensorEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
