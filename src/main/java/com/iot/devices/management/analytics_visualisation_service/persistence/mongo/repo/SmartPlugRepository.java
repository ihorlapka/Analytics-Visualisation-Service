package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.SMART_PLUG;

public interface SmartPlugRepository extends TelemetryRepository<SmartPlugEvent> {

    @Override
    default DeviceType getDeviceType() {
        return SMART_PLUG;
    }

    Flux<SmartPlugEvent> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to);

    Mono<SmartPlugEvent> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
