package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

public record SmartPlugDto(UUID deviceId,
                           Boolean isOn,
                           Float voltage,
                           Float current,
                           Float powerUsage,
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

    public Float getVoltage() {
        return voltage;
    }

    public Float getCurrent() {
        return current;
    }

    public Float getPowerUsage() {
        return powerUsage;
    }
}
