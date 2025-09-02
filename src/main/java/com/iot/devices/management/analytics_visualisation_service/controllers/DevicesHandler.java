package com.iot.devices.management.analytics_visualisation_service.controllers;

import com.iot.devices.management.analytics_visualisation_service.analytics.AnalyticRegistry;
import com.iot.devices.management.analytics_visualisation_service.mapping.EventToDtoMapper;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services.TelemetryService;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.Device;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.DeviceRepository;
import com.iot.devices.management.analytics_visualisation_service.stream.TelemetryStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static reactor.core.publisher.Flux.fromIterable;

@Component
@RequiredArgsConstructor
public class DevicesHandler {

    private static final String DEVICE_TYPE = "deviceType";
    private static final String DEVICE_ID = "deviceId";
    private static final String RATE = "rate";
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String STATUS = "status";

    private final TelemetryService telemetryService;
    private final AnalyticRegistry analyticRegistry;
    private final TelemetryStream telemetryStream;
    private final DeviceRepository deviceRepository;

    public Mono<ServerResponse> getHistory(ServerRequest request) {
        return Mono.zip(getIdMono(request), getInstantMono(request, FROM), getInstantMono(request, TO), getDeviceTypeMono(request))
                .flatMap(this::getTelemetriesMonoList)
                .flatMap(dtoList -> ServerResponse.ok().body(BodyInserters.fromValue(dtoList)));
    }

    public Mono<ServerResponse> getRealTimeTelemetry(ServerRequest request) {
        final Mono<Integer> rateMono = Mono.fromCallable(() -> Integer.parseInt(request.queryParam(RATE).orElseThrow()));
        return Mono.zip(getIdMono(request), rateMono, getDeviceTypeMono(request))
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .body(getRealTimeData(tuple), TelemetryDto.class));
    }

    public Mono<ServerResponse> getHistoryWithRealTimeData(ServerRequest request) {
        return Mono.zip(getIdMono(request), getInstantMono(request, FROM), Mono.fromCallable(Instant::now), getDeviceTypeMono(request))
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .body(combineHistoryAndRealTimeData(tuple), TelemetryDto.class));
    }

    public Mono<ServerResponse> getAnalytics(ServerRequest request) {
        return Mono.zip(getIdMono(request), getInstantMono(request, FROM), getInstantMono(request, TO), getDeviceTypeMono(request))
                .flatMap( tuple -> getTelemetriesMonoList(tuple)
                        .map(telemetries -> analyticRegistry.getProvider(tuple.getT4()).calculate(telemetries)))
                .flatMap(analytic -> ServerResponse.ok().body(BodyInserters.fromValue(analytic)));
    }

    public Mono<ServerResponse> getAmountOfDevicesPerManufacturer(ServerRequest request) {
        return deviceRepository.findAll().collectList()
                .map(devices -> devices.stream()
                        .collect(Collectors.groupingBy(Device::getManufacturer, counting())))
                .flatMap(result -> ServerResponse.ok().body(BodyInserters.fromValue(result)));
    }

    public Mono<ServerResponse> getAmountOfDevicesWithStatus(ServerRequest request) {
        return deviceRepository.findAll().collectList()
                .map(devices -> devices.stream()
                        .filter(device -> request.queryParam(STATUS)
                                .map(DeviceStatus::valueOf)
                                .map(status -> status.equals(device.getStatus()))
                                .orElseThrow(() -> new IllegalArgumentException("param 'status' is not present in request")))
                        .collect(Collectors.groupingBy(Device::getManufacturer, counting())))
                .flatMap(result -> ServerResponse.ok().body(BodyInserters.fromValue(result)));
    }

    private Mono<Instant> getInstantMono(ServerRequest request, String period) {
        return Mono.fromCallable(() -> Instant.parse(request.queryParam(period)
                .orElseThrow()));
    }

    private Mono<UUID> getIdMono(ServerRequest request) {
        return Mono.fromCallable(() -> UUID.fromString(request.pathVariable(DEVICE_ID)));
    }

    private Mono<DeviceType> getDeviceTypeMono(ServerRequest request) {
        return Mono.fromCallable(() -> request.queryParam(DEVICE_TYPE)
                .map(DeviceType::getType)
                .orElseThrow());
    }

    private Mono<List<TelemetryDto>> getTelemetriesMonoList(Tuple4<UUID, Instant, Instant, DeviceType> tuple) {
        return telemetryService.findByDeviceIdAndLastUpdatedBetween(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4())
                .map(EventToDtoMapper::mapToDto)
                .collectList();
    }

    private Flux<TelemetryDto> getRealTimeData(Tuple3<UUID, Integer, DeviceType> tuple) {
        return telemetryStream.getStream(tuple.getT3(), tuple.getT1())
                .limitRate(tuple.getT2());
    }

    private Flux<TelemetryDto> combineHistoryAndRealTimeData(Tuple4<UUID, Instant, Instant, DeviceType> tuple) {
        return getTelemetriesMonoList(tuple)
                .flatMapMany(history -> fromIterable(history)
                        .concatWith(telemetryStream.getStream(tuple.getT4(), tuple.getT1())))
                .distinct();
    }
}
