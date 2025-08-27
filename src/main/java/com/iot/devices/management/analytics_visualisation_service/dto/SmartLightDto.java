package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;

import java.time.Instant;
import java.util.UUID;


public record SmartLightDto(
        UUID deviceId,
        Boolean isOn,
        Integer brightness,
        String colour,
        String mode,
        Float powerConsumption,
        DeviceStatus status,
        String firmwareVersion,
        Instant lastUpdated) {
}
