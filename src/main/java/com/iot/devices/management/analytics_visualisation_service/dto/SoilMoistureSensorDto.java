package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "SoilMoistureSensorTelemetry", description = "Soil Moisture Sensor Telemetry")
public record SoilMoistureSensorDto(
        UUID deviceId,
        Float moisturePercentage,
        Float soilTemperature,
        Integer batteryLevel,
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

    public Float getMoisturePercentage() {
        return moisturePercentage;
    }

    public Float getSoilTemperature() {
        return soilTemperature;
    }
}
