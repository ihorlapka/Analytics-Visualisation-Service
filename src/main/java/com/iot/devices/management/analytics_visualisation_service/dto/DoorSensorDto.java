package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DoorState;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "DoorSensorTelemetry", description = "Door Sensor Telemetry")
public record DoorSensorDto(
        @NonNull UUID deviceId,
        @Nullable DoorState doorState,
        @Nullable Integer batteryLevel,
        @Nullable Boolean tamperAlert,
        @Nullable DeviceStatus status,
        @Nullable Instant lastOpened,
        @Nullable String firmwareVersion,
        @NonNull Instant lastUpdated) implements TelemetryDto {

    @Override
    public UUID getDeviceId() {
        return deviceId;
    }

    @Override
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public Instant getLastOpened() {
        return lastOpened;
    }

    @Override
    public DeviceStatus getStatus() {
        return status;
    }

    public Boolean getTamperAlert() {
        return tamperAlert;
    }
}
