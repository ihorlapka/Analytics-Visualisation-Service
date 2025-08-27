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
                           Instant lastUpdated) {
}
