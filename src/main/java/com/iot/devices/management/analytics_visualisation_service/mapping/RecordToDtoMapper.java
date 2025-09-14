package com.iot.devices.management.analytics_visualisation_service.mapping;

import com.iot.devices.*;
import com.iot.devices.management.analytics_visualisation_service.dto.*;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DoorState;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.ThermostatMode;
import lombok.experimental.UtilityClass;

import java.util.UUID;

import static java.util.Optional.ofNullable;

@UtilityClass
public class RecordToDtoMapper {

    public static DoorSensorDto mapDoorSensor(DoorSensor ds) {
        return new DoorSensorDto(
                UUID.fromString(ds.getDeviceId()),
                ofNullable(ds.getDoorState())
                        .map(s -> DoorState.valueOf(s.name()))
                        .orElse(null),
                ds.getBatteryLevel(),
                ds.getTamperAlert(),
                ofNullable(ds.getStatus())
                        .map(s -> DeviceStatus.valueOf(s.name()))
                        .orElse(null),
                ds.getLastOpened(),
                ds.getFirmwareVersion(),
                ds.getLastUpdated());
    }

    public static ThermostatDto mapThermostat(Thermostat t) {
        return new ThermostatDto(
                UUID.fromString(t.getDeviceId()),
                t.getCurrentTemperature(),
                t.getTargetTemperature(),
                t.getHumidity(),
                ofNullable(t.getMode())
                        .map(mode -> ThermostatMode.valueOf(mode.name()))
                        .orElse(null),
                ofNullable(t.getStatus())
                        .map(s -> DeviceStatus.valueOf(s.name()))
                        .orElse(null),
                t.getFirmwareVersion(),
                t.getLastUpdated());
    }

    public static SmartLightDto mapSmartLight(SmartLight sl) {
        return new SmartLightDto(
                UUID.fromString(sl.getDeviceId()),
                sl.getIsOn(),
                sl.getBrightness(),
                sl.getColor(),
                ofNullable(sl.getMode())
                        .map(Enum::name)
                        .orElse(null),
                sl.getPowerConsumption(),
                ofNullable(sl.getStatus())
                        .map(s -> DeviceStatus.valueOf(s.name()))
                        .orElse(null),
                sl.getFirmwareVersion(),
                sl.getLastUpdated());
    }

    public static EnergyMeterDto mapEnergyMeter(EnergyMeter em) {
        return new EnergyMeterDto(
                UUID.fromString(em.getDeviceId()),
                em.getVoltage(),
                em.getCurrent(),
                em.getPower(),
                em.getEnergyConsumed(),
                ofNullable(em.getStatus())
                        .map(s -> DeviceStatus.valueOf(s.name()))
                        .orElse(null),
                em.getFirmwareVersion(),
                em.getLastUpdated());
    }

    public static SmartPlugDto mapSmartPlug(SmartPlug em) {
        return new SmartPlugDto(
                UUID.fromString(em.getDeviceId()),
                em.getIsOn(),
                em.getVoltage(),
                em.getCurrent(),
                em.getPowerUsage(),
                ofNullable(em.getStatus())
                        .map(s -> DeviceStatus.valueOf(s.name()))
                        .orElse(null),
                em.getFirmwareVersion(),
                em.getLastUpdated());
    }

    public static TemperatureSensorDto mapTemperatureSensor(TemperatureSensor ts) {
        return new TemperatureSensorDto(
                UUID.fromString(ts.getDeviceId()),
                ts.getTemperature(),
                ts.getHumidity(),
                ts.getPressure(),
                ofNullable(ts.getUnit())
                        .map(Enum::name)
                        .orElse(null),
                ofNullable(ts.getStatus())
                        .map(s -> DeviceStatus.valueOf(s.name()))
                        .orElse(null),
                ts.getFirmwareVersion(),
                ts.getLastUpdated());
    }

    public static SoilMoistureSensorDto mapSoilMoistureSensor(SoilMoistureSensor sms) {
        return new SoilMoistureSensorDto(
                UUID.fromString(sms.getDeviceId()),
                sms.getMoisturePercentage(),
                sms.getSoilTemperature(),
                sms.getBatteryLevel(),
                ofNullable(sms.getStatus())
                        .map(s -> DeviceStatus.valueOf(s.name()))
                        .orElse(null),
                sms.getFirmwareVersion(),
                sms.getLastUpdated());
    }
}
