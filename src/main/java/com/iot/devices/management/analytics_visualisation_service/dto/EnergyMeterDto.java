package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;

import java.time.Instant;
import java.util.UUID;


public record EnergyMeterDto(
        UUID deviceId,
        Float voltage,
        Float current,
        Float power,
        Float energyConsumed,
        DeviceStatus status,
        String firmwareVersion,
        Instant lastUpdated) {
}
