package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

public record TemperatureSensorDto(
        UUID deviceId,
        Float temperature,
        Float humidity,
        Float pressure,
        String unit,
        DeviceStatus status,
        String firmwareVersion,
        Instant lastUpdated) {
}
