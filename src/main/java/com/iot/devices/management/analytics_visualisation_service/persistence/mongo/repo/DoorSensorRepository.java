package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.DoorSensorEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.DOOR_SENSOR;

public interface DoorSensorRepository extends TelemetryRepository<DoorSensorEvent> {

    @Override
    default DeviceType getDeviceType() {
        return DOOR_SENSOR;
    }

    Flux<DoorSensorEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<DoorSensorEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
