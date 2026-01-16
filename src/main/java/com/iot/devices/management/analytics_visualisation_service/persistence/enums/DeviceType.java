package com.iot.devices.management.analytics_visualisation_service.persistence.enums;

import com.iot.devices.management.analytics_visualisation_service.dto.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.iot.devices.management.analytics_visualisation_service.cache.CacheConfig.*;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
    THERMOSTAT("Thermostat", THERMOSTAT_CACHE, ThermostatDto.class),
    DOOR_SENSOR("DoorSensor", DOOR_SENSOR_CACHE, DoorSensorDto.class),
    SMART_LIGHT("SmartLight", SMART_LIGHT_CACHE, SmartLightDto.class),
    ENERGY_METER("EnergyMeter", ENERGY_METER_CACHE, EnergyMeterDto.class),
    SMART_PLUG("SmartPlug", SMART_PLUG_CACHE, SmartPlugDto.class),
    TEMPERATURE_SENSOR("TemperatureSensor", TEMPERATURE_SENSOR_CACHE, TemperatureSensorDto.class),
    SOIL_MOISTURE_SENSOR("SoilMoistureSensor", SOIL_MOISTURE_SENSOR_CACHE, SoilMoistureSensorDto.class);

    private final String id;
    private final String cacheName;
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
