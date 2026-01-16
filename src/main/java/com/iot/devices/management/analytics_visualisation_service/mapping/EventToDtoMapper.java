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

    private static final Map<DeviceType, Function<? extends TelemetryEvent, ? extends TelemetryDto>> MAPPER_BY_TYPE = ImmutableMap.<DeviceType, Function<? extends TelemetryEvent, ? extends TelemetryDto>>builder()
            .put(DOOR_SENSOR, (Function<DoorSensorEvent, DoorSensorDto>) EventToDtoMapper::mapDoorSensor)
            .put(ENERGY_METER, (Function<EnergyMeterEvent, EnergyMeterDto>) EventToDtoMapper::mapEnergyMeter)
            .put(SMART_LIGHT, (Function<SmartLightEvent, SmartLightDto>) EventToDtoMapper::mapSmartLight)
            .put(SMART_PLUG, (Function<SmartPlugEvent, SmartPlugDto>) EventToDtoMapper::mapSmartPlug)
            .put(SOIL_MOISTURE_SENSOR, (Function<SoilMoistureSensorEvent, SoilMoistureSensorDto>) EventToDtoMapper::mapSoilMoistureSensor)
            .put(TEMPERATURE_SENSOR, (Function<TemperatureSensorEvent, TemperatureSensorDto>) EventToDtoMapper::mapTemperatureSensor)
            .put(THERMOSTAT, (Function<ThermostatEvent, ThermostatDto>) EventToDtoMapper::mapThermostat)
            .build();

    @SuppressWarnings("unchecked")
    public static TelemetryDto mapToDto(TelemetryEvent event) {
        final Function<TelemetryEvent, TelemetryDto> mapper = (Function<TelemetryEvent, TelemetryDto>) MAPPER_BY_TYPE.get(event.getDeviceType());
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper for class: " + event.getClass());
        }
        return mapper.apply(event);
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
}
