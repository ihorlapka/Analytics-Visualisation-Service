package com.iot.devices.management.analytics_visualisation_service.stream;

import com.iot.alerts.Alert;
import com.iot.devices.management.analytics_visualisation_service.dto.AlertDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.devices.management.analytics_visualisation_service.mapping.RecordToDtoMapper.mapToAlertDto;
import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class AlertStream {
    public static final int HISTORY_SIZE = 100;

    private final Map<UUID, Sinks.Many<AlertDto>> sinkByDeviceId = new ConcurrentHashMap<>();


    public Mono<Void> publish(Alert alert) {
        return Mono.just(sinkByDeviceId.compute(UUID.fromString(alert.getDeviceId()), (k, v) -> {
            if (v == null) {
                v = Sinks.many().replay().limit(HISTORY_SIZE);
            }
            v.tryEmitNext(mapToAlertDto(alert));
            return v;
        })).then();
    }

    public Flux<AlertDto> getStream(UUID deviceId) {
        return ofNullable(sinkByDeviceId.get(deviceId))
                .map(Sinks.Many::asFlux)
                .orElse(Flux.empty());
    }

    public void purge() {
        sinkByDeviceId.clear();
    }
}
