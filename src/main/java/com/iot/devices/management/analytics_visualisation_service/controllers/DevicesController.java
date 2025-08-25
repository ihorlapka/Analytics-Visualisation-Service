package com.iot.devices.management.analytics_visualisation_service.controllers;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TelemetryEvent;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo.SmartPlugRepository;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.DeviceRepository;
import com.iot.devices.management.analytics_visualisation_service.stream.TelemetryStream;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class DevicesController {

    private final DeviceRepository deviceRepository;
    private final SmartPlugRepository smartPlugRepository;
    private final TelemetryStream telemetryStream;


    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello from Analytics Service!");
    }

    @GetMapping("/devices/{deviceId}")
    public Flux<SmartPlugEvent> getDevice(@PathVariable UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .flatMapMany(x -> smartPlugRepository.findAllById(List.of(deviceId)))
                .takeLast(5);
    }

//    @GetMapping("/devices/{deviceId}")
//    public Flux<SmartPlugEvent> getHistoricalData(@PathVariable UUID deviceId, @RequestParam Instant from, @RequestParam Instant to) {
//        return smartPlugRepository.findByDeviceIdAndLastUpdatedBetween(deviceId, from, to);
//    }

    @GetMapping(value = "/devices/{deviceId}", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<? extends TelemetryEvent> getHistoricalAndRealTimeData(@PathVariable UUID deviceId, @RequestParam Instant from) {
       return Flux.merge(smartPlugRepository.findByDeviceIdAndLastUpdatedBetween(deviceId, from, now()),
                       (telemetryStream.getStream(SmartPlugEvent.class, deviceId)))
               .distinct();
    }
}
