package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DoorState;

import java.time.Instant;
import java.util.UUID;


public record DoorSensorDto(
        UUID deviceId,
        DoorState doorState,
        Integer batteryLevel,
        Boolean tamperAlert,
        DeviceStatus status,
        Instant lastOpened,
        String firmwareVersion,
        Instant lastUpdated) {
}
