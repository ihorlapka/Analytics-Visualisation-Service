package com.iot.devices.management.analytics_visualisation_service.stream;

import com.iot.devices.management.analytics_visualisation_service.dto.*;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache.TelemetryCachingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.*;
import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class TelemetryStream {

    public static final int HISTORY_SIZE = 100;

    private final Map<DeviceType, Sinks.Many<TelemetryDto>> sinkByClass = new ConcurrentHashMap<>();

    private final TelemetryCachingRepository telemetryCachingRepository;

    public Mono<Void> publish(SpecificRecord record) {
        return Mono.just(sinkByClass.compute(getType(record.getSchema().getName()), (k, v) -> {
            if (v == null) {
                v = Sinks.many().replay().limit(HISTORY_SIZE);
            }
            v.tryEmitNext(telemetryCachingRepository.mapAndCacheTelemetryDto(record, k));
            return v;
        })).then();
    }

    @SuppressWarnings("unchecked")
    public <T extends TelemetryDto> Flux<T> getStream(DeviceType deviceType, UUID deviceId) {
        return ofNullable(sinkByClass.get(deviceType))
                .map(eventMany -> eventMany
                        .asFlux()
                        .filter(event -> event.getDeviceId().equals(deviceId))
                        .map(event -> (T) event)
                )
                .orElse(Flux.empty());
    }

    public void purge() {
        sinkByClass.clear();
    }
}
