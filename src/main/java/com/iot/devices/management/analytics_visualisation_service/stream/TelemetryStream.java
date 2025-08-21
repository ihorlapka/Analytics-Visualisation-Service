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
import java.util.concurrent.ConcurrentHashMap;

import static com.iot.devices.management.analytics_visualisation_service.mapping.EventsMapper.*;
import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class TelemetryStream {

    private final Map<Class<? extends TelemetryEvent>, Sinks.Many<TelemetryEvent>> sinkByClass = new ConcurrentHashMap<>();

    public Mono<Void> publish(SpecificRecord record) {
        return Mono.fromRunnable(() -> sinkByClass.compute(mapClass(record), (k, v) -> {
            if (v == null) {
                v = Sinks.many().replay().limit(100);
            }
            v.tryEmitNext(map(record));
            return v;
        })).then();
    }

    public <T extends TelemetryEvent> Flux<? extends TelemetryEvent> getStream(Class<T> clazz) {
        return ofNullable(sinkByClass.get(clazz))
                .map(Sinks.Many::asFlux)
                .orElse(Flux.empty());
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
