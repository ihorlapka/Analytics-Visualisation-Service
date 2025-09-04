package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "SmartLightTelemetry", description = "Smart Light Telemetry")
public record SmartLightDto(
        UUID deviceId,
        Boolean isOn,
        Integer brightness,
        String colour,
        String mode,
        Float powerConsumption,
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

    public Integer getBrightness() {
        return brightness;
    }

    public Float getPowerConsumption() {
        return powerConsumption;
    }
}
