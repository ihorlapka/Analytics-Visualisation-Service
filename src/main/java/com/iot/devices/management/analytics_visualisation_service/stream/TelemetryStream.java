package com.iot.devices.management.analytics_visualisation_service.stream;

import com.iot.devices.*;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.devices.management.analytics_visualisation_service.mapping.EventsMapper.*;
import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class TelemetryStream {

    public static final int HISTORY_SIZE = 100;

    private final Map<Class<? extends TelemetryEvent>, Sinks.Many<TelemetryEvent>> sinkByClass = new ConcurrentHashMap<>();

    public Mono<Void> publish(SpecificRecord record) {
        return Mono.just(sinkByClass.compute(mapClass(record), (k, v) -> {
            if (v == null) {
                v = Sinks.many().replay().limit(HISTORY_SIZE);
            }
            v.tryEmitNext(map(record));
            return v;
        })).then();
    }

    @SuppressWarnings("unchecked")
    public <T extends TelemetryEvent> Flux<T> getStream(Class<T> clazz, UUID deviceId) {
        return ofNullable(sinkByClass.get(clazz))
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

    private Class<? extends TelemetryEvent> mapClass(SpecificRecord record) {
        return switch (record) {
            case DoorSensor ignored -> DoorSensorEvent.class;
            case EnergyMeter ignored -> EnergyMeterEvent.class;
            case SmartLight ignored -> SmartLightEvent.class;
            case SmartPlug ignored -> SmartPlugEvent.class;
            case SoilMoistureSensor ignored -> SoilMoistureSensorEvent.class;
            case TemperatureSensor ignored -> TemperatureSensorEvent.class;
            case Thermostat ignored -> ThermostatEvent.class;
            default -> throw new IllegalArgumentException("Unknown device type");
        };
    }

    private TelemetryEvent map(SpecificRecord record) {
        return switch (record) {
            case DoorSensor ds -> mapDoorSensor(ds);
            case EnergyMeter em -> mapEnergyMeter(em);
            case SmartLight sl -> mapSmartLight(sl);
            case SmartPlug sp -> mapSmartPlug(sp);
            case SoilMoistureSensor sms -> mapSoilMoistureSensor(sms);
            case TemperatureSensor ts -> mapTemperatureSensor(ts);
            case Thermostat t -> mapThermostat(t);
            default -> throw new IllegalArgumentException("Unknown device type");
        };
    }
}
