package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartLightEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.SMART_LIGHT;

public interface SmartLightRepository extends TelemetryRepository<SmartLightEvent> {

    @Override
    default DeviceType getDeviceType() {
        return SMART_LIGHT;
    }

    Flux<SmartLightEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<SmartLightEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
