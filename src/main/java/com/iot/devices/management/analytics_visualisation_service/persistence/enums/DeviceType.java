package com.iot.devices.management.analytics_visualisation_service.persistence.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    THERMOSTAT("Thermostat"),
    DOOR_SENSOR("DoorSensor"),
    SMART_LIGHT("SmartLight"),
    ENERGY_METER("EnergyMeter"),
    SMART_PLUG("SmartPlug"),
    TEMPERATURE_SENSOR("TemperatureSensor"),
    SOIL_MOISTURE_SENSOR("SoilMoistureSensor");

    private final String id;

    public static DeviceType getType(String name) {
        for (DeviceType type : values()) {
            if (type.getId().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("DeviceType " + name + " is not supported!");
    }
}
