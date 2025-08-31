package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.ThermostatMode;

import java.time.Instant;
import java.util.UUID;

public record ThermostatDto(
        UUID deviceId,
        Float currentTemperature,
        Float targetTemperature,
        Float humidity,
        ThermostatMode mode,
        DeviceStatus status,
        String firmwareVersion,
        Instant lastUpdated) implements TelemetryDto {

    @Override
    public UUID getDeviceId() {
        return deviceId;
    }

    @Override
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public DeviceStatus getStatus() {
        return status;
    }

    public Float getCurrentTemperature() {
        return currentTemperature;
    }

    public Float getTargetTemperature() {
        return targetTemperature;
    }

    public Float getHumidity() {
        return humidity;
    }
}
