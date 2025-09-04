package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;


@Schema(name = "EnergyMeterTelemetry", description = "Energy Meter Telemetry")
public record EnergyMeterDto(
        UUID deviceId,
        Float voltage,
        Float current,
        Float power,
        Float energyConsumed,
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

    public Float getPower() {
        return power;
    }

    public Float getEnergyConsumed() {
        return energyConsumed;
    }
}
