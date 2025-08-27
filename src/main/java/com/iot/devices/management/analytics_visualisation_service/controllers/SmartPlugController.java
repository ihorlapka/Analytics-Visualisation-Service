package com.iot.devices.management.analytics_visualisation_service.controllers;

import com.iot.devices.management.analytics_visualisation_service.analytics.SmartPlugAnalytic;
import com.iot.devices.management.analytics_visualisation_service.dto.DtoMapper;
import com.iot.devices.management.analytics_visualisation_service.dto.SmartPlugDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
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
import static reactor.core.publisher.Flux.fromIterable;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class SmartPlugController {

    //TODO: add alerts endpoints!
    //TODO: add Swagger

    private final DeviceRepository deviceRepository;
//    private final UserRepository userRepository;
    private final SmartPlugRepository smartPlugRepository;
    private final TelemetryStream telemetryStream;
    private final SmartPlugAnalytic analytic;


//    @GetMapping("/devices/{deviceId}")
//    public Flux<SmartPlugEvent> getDevice(@PathVariable UUID deviceId) {
//        return deviceRepository.findById(deviceId)
//                .flatMapMany(x -> smartPlugRepository.findAllById(List.of(deviceId)))
//                .takeLast(5);
//    }

    @GetMapping("/devices/{deviceId}/history")
    public Mono<List<SmartPlugDto>> getHistory(@PathVariable UUID deviceId, @RequestParam Instant from, @RequestParam Instant to) {
        return smartPlugRepository.findByDeviceIdAndLastUpdatedBetween(deviceId, from, to).collectList()
                .map(list -> list.stream().map(DtoMapper::mapSmartPlug).toList());
    }

    @GetMapping(path = "/devices/{deviceId}/realTime", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<SmartPlugDto> getRealTimeData(@PathVariable UUID deviceId, @RequestParam int rate) {
        return telemetryStream.getStream(SmartPlugEvent.class, deviceId)
                .map(DtoMapper::mapSmartPlug)
                .limitRate(rate);
    }

    @GetMapping(path = "/devices/{deviceId}historyWithRealTime", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<SmartPlugDto> getHistoricalAndRealTimeData(@PathVariable UUID deviceId, @RequestParam Instant from) {
       return smartPlugRepository.findByDeviceIdAndLastUpdatedBetween(deviceId, from, now()).collectList()
               .flatMapMany(history -> fromIterable(history).concatWith(telemetryStream.getStream(SmartPlugEvent.class, deviceId)))
               .map(DtoMapper::mapSmartPlug);
    }

    @PostMapping("/devices/{deviceId}/analytics")
    public Flux<SmartPlugAnalytic> getAnalytics(List<UUID> deviceIds, @RequestParam Instant from, @RequestParam Instant to) {
        return fromIterable(deviceIds)
                .map(deviceId -> smartPlugRepository.findByDeviceIdAndLastUpdatedBetween(deviceId, from, to).collectList())
                .flatMap(eventsMono -> eventsMono.map(analytic::calculate));
    }
}
