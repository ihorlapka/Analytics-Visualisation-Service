package com.iot.devices.management.analytics_visualisation_service.stream;

import com.iot.devices.*;
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
        return Mono.just(sinkByClass.compute(getType(record), (k, v) -> {
            if (v == null) {
                v = Sinks.many().replay().limit(HISTORY_SIZE);
            }
            v.tryEmitNext(mapAndPutInCache(record));
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

    private TelemetryDto mapAndPutInCache(SpecificRecord record) {
        return switch (record) {
            case DoorSensor ds -> telemetryCachingRepository.mapAndCacheDoorSensorDto(ds);
            case EnergyMeter em -> telemetryCachingRepository.mapAndCacheEnergyMeterDto(em);
            case SmartLight sl -> telemetryCachingRepository.mapAndCacheSmartLightDto(sl);
            case SmartPlug sp -> telemetryCachingRepository.mapAndCacheSmartPlugDto(sp);
            case SoilMoistureSensor sms -> telemetryCachingRepository.getSoilMoistureSensorDto(sms);
            case TemperatureSensor ts -> telemetryCachingRepository.mapAndCacheTemperatureSensorDto(ts);
            case Thermostat t -> telemetryCachingRepository.mapAndCacheThermostatDto(t);
            default -> throw new IllegalArgumentException("Unknown device type");
        };
    }

    private DeviceType getType(SpecificRecord record) {
        return switch (record) {
            case DoorSensor ignored -> DOOR_SENSOR;
            case EnergyMeter ignored -> ENERGY_METER;
            case SmartLight ignored -> SMART_LIGHT;
            case SmartPlug ignored -> SMART_PLUG;
            case SoilMoistureSensor ignored -> SOIL_MOISTURE_SENSOR;
            case TemperatureSensor ignored -> TEMPERATURE_SENSOR;
            case Thermostat ignored -> THERMOSTAT;
            default -> throw new IllegalArgumentException("Unknown device type");
        };
    }
}
