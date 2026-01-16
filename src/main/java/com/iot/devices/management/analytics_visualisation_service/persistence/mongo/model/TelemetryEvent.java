package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model;


import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;

import java.time.Instant;
import java.util.UUID;

public interface TelemetryEvent {

    UUID getDeviceId();
    Instant getLastUpdated();
    DeviceStatus getStatus();
    DeviceType getDeviceType();
}
