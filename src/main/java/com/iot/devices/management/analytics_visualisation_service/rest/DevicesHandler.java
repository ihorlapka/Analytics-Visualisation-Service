package com.iot.devices.management.analytics_visualisation_service.rest;

import com.iot.devices.management.analytics_visualisation_service.analytics.AnalyticRegistry;
import com.iot.devices.management.analytics_visualisation_service.dto.AlertDto;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.mapping.EventToDtoMapper;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.UserRole;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache.TelemetryCachingRepository;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo.AlertsRepository;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.Device;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.DeviceRepository;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.UserRepository;
import com.iot.devices.management.analytics_visualisation_service.security.AccessNotAllowed;
import com.iot.devices.management.analytics_visualisation_service.stream.AlertStream;
import com.iot.devices.management.analytics_visualisation_service.stream.TelemetryStream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;

import java.security.InvalidParameterException;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.UserRole.SUPER_ADMIN;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.counting;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static reactor.core.publisher.Flux.fromIterable;

@Service
@RequiredArgsConstructor
public class DevicesHandler {

    static final String DEVICE_TYPE = "deviceType";
    static final String DEVICE_ID = "deviceId";
    static final String RATE = "rate";
    static final String FROM = "from";
    static final String TO = "to";
    static final String STATUS = "status";

    private final AnalyticRegistry analyticRegistry;
    private final TelemetryStream telemetryStream;
    private final DeviceRepository deviceRepository;
    private final TelemetryCachingRepository telemetryCachingRepository;
    private final UserRepository userRepository;
    private final AlertsRepository alertsRepository;
    private final AlertStream alertStream;

    public Mono<ServerResponse> getTelemetryHistory(ServerRequest request) {
        return Mono.zip(getId(request), getInstant(request, FROM), getInstant(request, TO), getDeviceType(request), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT5(), tuple.getT1()))
                .flatMap(this::getTelemetriesMonoList)
                .flatMap(dtoList -> ServerResponse.ok().body(BodyInserters.fromValue(dtoList)))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getRealTimeTelemetry(ServerRequest request) {
        return Mono.zip(getId(request), getRate(request), getDeviceType(request), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT4(), tuple.getT1()))
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .body(getTelemetryRealTimeData(tuple), TelemetryDto.class))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getTelemetryHistoryWithRealTimeData(ServerRequest request) {
        return Mono.zip(getId(request), getInstant(request, FROM), Mono.just(Instant.now()), getDeviceType(request), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT5(), tuple.getT1()))
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .body(combineTelemetryHistoryAndRealTimeData(tuple), TelemetryDto.class))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getAnalytics(ServerRequest request) {
        return Mono.zip(getId(request), getInstant(request, FROM), getInstant(request, TO), getDeviceType(request), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT5(), tuple.getT1()))
                .flatMap( tuple -> getTelemetriesMonoList(tuple)
                        .map(telemetries -> analyticRegistry.getProvider(tuple.getT4()).calculate(telemetries)))
                .flatMap(analytic -> ServerResponse.ok().body(BodyInserters.fromValue(analytic)))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getLastTelemetry(ServerRequest request) {
        return Mono.zip(getId(request), getDeviceType(request), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT3(), tuple.getT1()))
                .flatMap(tuple -> telemetryCachingRepository.findLatestTelemetry(tuple.getT1(), tuple.getT2()))
                .flatMap(result -> ServerResponse.ok().body(BodyInserters.fromValue(result)))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getAmountOfDevicesPerManufacturer(ServerRequest request) {
        return deviceRepository.findAll().collectList()
                .map(devices -> devices.stream()
                        .collect(Collectors.groupingBy(Device::getManufacturer, counting())))
                .flatMap(result -> ServerResponse.ok().body(BodyInserters.fromValue(result)));
    }

    public Mono<ServerResponse> getAmountOfDevicesPerStatus(ServerRequest request) {
        return deviceRepository.findAll().collectList()
                .map(devices -> devices.stream()
                        .filter(device -> request.queryParam(STATUS)
                                .map(DeviceStatus::valueOf)
                                .map(status -> status.equals(device.getStatus()))
                                .orElseThrow(() -> new IllegalArgumentException("param 'status' is not present in request")))
                        .collect(Collectors.groupingBy(Device::getManufacturer, counting())))
                .flatMap(result -> ServerResponse.ok().body(BodyInserters.fromValue(result)));
    }

    public Mono<ServerResponse> getAlertsHistory(ServerRequest request) {
        return Mono.zip(getId(request), getInstant(request, FROM), getInstant(request, TO), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT4(), tuple.getT1()))
                .flatMap(this::getAlertsMonoList)
                .flatMap(dtoList -> ServerResponse.ok().body(BodyInserters.fromValue(dtoList)))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getRealTimeAlerts(ServerRequest request) {
        return Mono.zip(getId(request), getRate(request), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT3(), tuple.getT1()))
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .body(getAlertRealTimeData(tuple), AlertDto.class))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getHistoryWithRealTimeAlerts(ServerRequest request) {
        return Mono.zip(getId(request), getInstant(request, FROM), Mono.just(Instant.now()), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT4(), tuple.getT1()))
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .body(combineAlertsHistoryAndRealTimeData(tuple), AlertDto.class))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    public Mono<ServerResponse> getLastAlert(ServerRequest request) {
        return Mono.zip(getId(request), getDeviceType(request), request.principal())
                .filterWhen(tuple ->  hasPermission(tuple.getT3(), tuple.getT1()))
                .flatMap(tuple -> alertsRepository.findFirstByDeviceIdOrderByTimestampDesc(tuple.getT1()))
                .flatMap(result -> ServerResponse.ok().body(BodyInserters.fromValue(result)))
                .switchIfEmpty(Mono.error(new AccessNotAllowed("Permission denied to resource")));
    }

    private Mono<Instant> getInstant(ServerRequest request, String period) {
        return Mono.justOrEmpty(request.queryParam(period))
                .map(LocalDateTime::parse)
                .map(localDateTime -> localDateTime.toInstant(ZoneOffset.UTC))
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Missing parameter: " + period)));
    }

    private Mono<UUID> getId(ServerRequest request) {
        return Mono.just(request.pathVariable(DEVICE_ID))
                .map(UUID::fromString)
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Invalid deviceId format"));
    }

    private Mono<DeviceType> getDeviceType(ServerRequest request) {
        return Mono.justOrEmpty(request.queryParam(DEVICE_TYPE))
                .map(DeviceType::getType)
                .switchIfEmpty(Mono.error(() -> new InvalidParameterException("Missing parameter: " + DEVICE_TYPE)));
    }

    private Mono<Integer> getRate(ServerRequest request) {
        return Mono.justOrEmpty(request.queryParam(RATE))
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.error(() -> new InvalidParameterException("Missing parameter: " + RATE)));
    }

    private Mono<List<TelemetryDto>> getTelemetriesMonoList(Tuple4<UUID, Instant, Instant, DeviceType> tuple) {
        return telemetryCachingRepository.getFromCacheOrDb(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4());
    }

    private Flux<TelemetryDto> getTelemetryRealTimeData(Tuple3<UUID, Integer, DeviceType> tuple) {
        return telemetryStream.getStream(tuple.getT3(), tuple.getT1()).limitRate(tuple.getT2());
    }

    private Flux<TelemetryDto> combineTelemetryHistoryAndRealTimeData(Tuple4<UUID, Instant, Instant, DeviceType> tuple) {
        return getTelemetriesMonoList(tuple)
                .flatMapMany(history -> fromIterable(history).concatWith(telemetryStream.getStream(tuple.getT4(), tuple.getT1())))
                .distinct();
    }

    private <P extends Principal> Mono<List<AlertDto>> getAlertsMonoList(Tuple4<UUID, Instant, Instant, P> tuple) {
        return alertsRepository.findByDeviceIdAndTimestampBetween(tuple.getT1(), tuple.getT2(), tuple.getT3())
                .map(EventToDtoMapper::mapToAlertDto)
                .collectList();
    }

    private Flux<AlertDto> getAlertRealTimeData(Tuple2<UUID, Integer> tuple) {
        return alertStream.getStream(tuple.getT1()).limitRate(tuple.getT2());
    }

    private <P extends Principal> Flux<AlertDto> combineAlertsHistoryAndRealTimeData(Tuple4<UUID, Instant, Instant, P> tuple) {
        return getAlertsMonoList(tuple)
                .flatMapMany(history -> fromIterable(history).concatWith(alertStream.getStream(tuple.getT1())))
                .distinct();
    }

    private <P extends Principal> Mono<Boolean> hasPermission(P principal, UUID deviceId) {
        final Authentication auth = (Authentication) principal;
        if (isSuperAdmin(auth)) {
            return Mono.just(true);
        }
        return userRepository.findByDeviceId(deviceId)
                .map(desiredUser -> {
                    if (auth.getName().equals(desiredUser.getUsername())) {
                        return true;
                    }
                    return getAuthUserRole(auth).getLevel() < desiredUser.getUserRole().getLevel();
                })
                .defaultIfEmpty(false);
    }

    private boolean isSuperAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(SUPER_ADMIN.getRoleName()));
    }

    private UserRole getAuthUserRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::findByRole)
                .min(comparingInt(UserRole::getLevel))
                .orElseThrow(() -> new RuntimeException("UserRole in authentication is not found!"));
    }
}
