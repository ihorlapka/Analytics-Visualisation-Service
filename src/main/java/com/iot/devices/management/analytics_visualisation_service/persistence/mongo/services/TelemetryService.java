package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services;

import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.mapping.EventToDtoMapper;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TelemetryEvent;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

@Service
public class TelemetryService {

    private final Map<DeviceType, TelemetryRepository<? extends TelemetryEvent>> repositoryByType;


    public TelemetryService(List<TelemetryRepository<? extends TelemetryEvent>> repositories) {
        this.repositoryByType = repositories.stream()
                .peek(repo -> {
                    if (repo.getDeviceType() == null) {
                        throw new IllegalStateException("Repository " + repo.getClass().getName() + " must provide a DeviceType!");
                    }
                }).collect(toUnmodifiableMap(TelemetryRepository::getDeviceType, Function.identity()));
    }


    public Mono<List<TelemetryDto>> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to, DeviceType deviceType) {
        return repositoryByType.get(deviceType).findByDeviceIdAndLastUpdatedBetween(deviceId, from, to)
                .map(EventToDtoMapper::mapToDto)
                .collectList();
    }

    public Mono<TelemetryDto> findLatestTelemetry(UUID deviceId, DeviceType deviceType) {
        return repositoryByType.get(deviceType).findFirstByDeviceIdOrderByLastUpdatedDesc(deviceId)
                .map(EventToDtoMapper::mapToDto);
    }
}
