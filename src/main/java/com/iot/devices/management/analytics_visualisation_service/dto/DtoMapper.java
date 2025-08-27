package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

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
}
