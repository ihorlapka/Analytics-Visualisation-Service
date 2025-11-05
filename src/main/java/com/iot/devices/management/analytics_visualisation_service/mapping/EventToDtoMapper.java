package com.iot.devices.management.analytics_visualisation_service.mapping;

import com.google.common.collect.ImmutableMap;
import com.iot.devices.management.analytics_visualisation_service.dto.*;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.SeverityLevel;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.*;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.function.Function;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.*;

@UtilityClass
public class EventToDtoMapper {

    private static final Map<DeviceType, TelemetryMapper<?, ?>> MAPPER_BY_TYPE = ImmutableMap.<DeviceType, TelemetryMapper<?, ?>>builder()
            .put(DOOR_SENSOR, (TelemetryMapper<DoorSensorEvent, DoorSensorDto>) EventToDtoMapper::mapDoorSensor)
            .put(ENERGY_METER, (TelemetryMapper<EnergyMeterEvent, EnergyMeterDto>) EventToDtoMapper::mapEnergyMeter)
            .put(SMART_LIGHT, (TelemetryMapper<SmartLightEvent, SmartLightDto>) EventToDtoMapper::mapSmartLight)
            .put(SMART_PLUG, (TelemetryMapper<SmartPlugEvent, SmartPlugDto>) EventToDtoMapper::mapSmartPlug)
            .put(SOIL_MOISTURE_SENSOR, (TelemetryMapper<SoilMoistureSensorEvent, SoilMoistureSensorDto>) EventToDtoMapper::mapSoilMoistureSensor)
            .put(TEMPERATURE_SENSOR, (TelemetryMapper<TemperatureSensorEvent, TemperatureSensorDto>) EventToDtoMapper::mapTemperatureSensor)
            .put(THERMOSTAT, (TelemetryMapper<ThermostatEvent, ThermostatDto>) EventToDtoMapper::mapThermostat)
            .build();

    @SuppressWarnings("unchecked")
    public static TelemetryDto mapToDto(TelemetryEvent event) {
        final TelemetryMapper<TelemetryEvent, TelemetryDto> mapper = (TelemetryMapper<TelemetryEvent, TelemetryDto>) MAPPER_BY_TYPE.get(getDeviceType(event));
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for class: " + event.getClass());
        }
        return mapper.apply(event);
    }

    private DeviceType getDeviceType(TelemetryEvent event) {
        return switch (event) {
            case DoorSensorEvent ds -> DOOR_SENSOR;
            case EnergyMeterEvent em -> ENERGY_METER;
            case SmartLightEvent sl -> SMART_LIGHT;
            case SmartPlugEvent sp -> SMART_PLUG;
            case SoilMoistureSensorEvent sms -> SOIL_MOISTURE_SENSOR;
            case TemperatureSensorEvent ts -> TEMPERATURE_SENSOR;
            case ThermostatEvent t -> THERMOSTAT;
            default -> throw new IllegalArgumentException("Unknown event type");
        };
    }

    public static DoorSensorDto mapDoorSensor(DoorSensorEvent ds) {
        return new DoorSensorDto(
                ds.getDeviceId(),
                ds.getDoorState(),
                ds.getBatteryLevel(),
                ds.getTamperAlert(),
                ds.getStatus(),
                ds.getLastOpened(),
                ds.getFirmwareVersion(),
                ds.getLastUpdated());
    }

    public static ThermostatDto mapThermostat(ThermostatEvent t) {
        return new ThermostatDto(
                t.getDeviceId(),
                t.getCurrentTemperature(),
                t.getTargetTemperature(),
                t.getHumidity(),
                t.getMode(),
                t.getStatus(),
                t.getFirmwareVersion(),
                t.getLastUpdated());
    }

    public static SmartLightDto mapSmartLight(SmartLightEvent sl) {
        return new SmartLightDto(
                sl.getDeviceId(),
                sl.getIsOn(),
                sl.getBrightness(),
                sl.getColour(),
                sl.getMode(),
                sl.getPowerConsumption(),
                sl.getStatus(),
                sl.getFirmwareVersion(),
                sl.getLastUpdated());
    }

    public static EnergyMeterDto mapEnergyMeter(EnergyMeterEvent em) {
        return new EnergyMeterDto(
                em.getDeviceId(),
                em.getVoltage(),
                em.getCurrent(),
                em.getPower(),
                em.getEnergyConsumed(),
                em.getStatus(),
                em.getFirmwareVersion(),
                em.getLastUpdated());
    }

    public static SmartPlugDto mapSmartPlug(SmartPlugEvent em) {
        return new SmartPlugDto(
                em.getDeviceId(),
                em.getIsOn(),
                em.getVoltage(),
                em.getCurrent(),
                em.getPowerUsage(),
                em.getStatus(),
                em.getFirmwareVersion(),
                em.getLastUpdated());
    }

    public static TemperatureSensorDto mapTemperatureSensor(TemperatureSensorEvent ts) {
        return new TemperatureSensorDto(
                ts.getDeviceId(),
                ts.getTemperature(),
                ts.getHumidity(),
                ts.getPressure(),
                ts.getUnit(),
                ts.getStatus(),
                ts.getFirmwareVersion(),
                ts.getLastUpdated());
    }

    public static SoilMoistureSensorDto mapSoilMoistureSensor(SoilMoistureSensorEvent sms) {
        return new SoilMoistureSensorDto(
                sms.getDeviceId(),
                sms.getMoisturePercentage(),
                sms.getSoilTemperature(),
                sms.getBatteryLevel(),
                sms.getStatus(),
                sms.getFirmwareVersion(),
                sms.getLastUpdated());
    }

    public static AlertDto mapToAlertDto(AlertEvent alertEvent) {
        return new AlertDto(alertEvent.getAlertId(),
                alertEvent.getDeviceId(),
                alertEvent.getRuleId(),
                SeverityLevel.valueOf(alertEvent.getSeverity().name()),
                alertEvent.getTimestamp(),
                alertEvent.getMessage(),
                alertEvent.getActualValue());
    }

    @FunctionalInterface
    interface TelemetryMapper<E extends TelemetryEvent, D extends TelemetryDto> extends Function<E, D> {}
}
