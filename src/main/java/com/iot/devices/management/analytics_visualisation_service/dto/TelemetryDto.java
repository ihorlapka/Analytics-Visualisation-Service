package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

public interface TelemetryDto {
    UUID getDeviceId();
    Instant getLastUpdated();
    DeviceStatus getStatus();
}
