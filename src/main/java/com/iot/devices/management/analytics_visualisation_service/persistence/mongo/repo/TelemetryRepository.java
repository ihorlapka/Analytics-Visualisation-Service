package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TelemetryEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@NoRepositoryBean
public interface TelemetryRepository<T extends TelemetryEvent> extends ReactiveMongoRepository<T, UUID> {

    DeviceType getDeviceType();

    Flux<T> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant start, Instant end);

    Mono<T> findFirstByDeviceIdOrderByLastUpdatedDesc(UUID deviceId);
}
