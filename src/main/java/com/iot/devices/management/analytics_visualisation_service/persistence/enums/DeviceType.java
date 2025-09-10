package com.iot.devices.management.analytics_visualisation_service.persistence.enums;

import com.iot.devices.management.analytics_visualisation_service.dto.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    THERMOSTAT("Thermostat", ThermostatDto.class),
    DOOR_SENSOR("DoorSensor", DoorSensorDto.class),
    SMART_LIGHT("SmartLight", SmartLightDto.class),
    ENERGY_METER("EnergyMeter", EnergyMeterDto.class),
    SMART_PLUG("SmartPlug", SmartPlugDto.class),
    TEMPERATURE_SENSOR("TemperatureSensor", TemperatureSensorDto.class),
    SOIL_MOISTURE_SENSOR("SoilMoistureSensor", SoilMoistureSensorDto.class);

    private final String id;
    private final Class<? extends TelemetryDto> dtoClass;

    public static DeviceType getType(String name) {
        for (DeviceType type : values()) {
            if (type.getId().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("DeviceType " + name + " is not supported!");
    }
}
