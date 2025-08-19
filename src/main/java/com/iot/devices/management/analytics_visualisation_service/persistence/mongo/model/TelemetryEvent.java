package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model;

import java.time.Instant;
import java.util.UUID;

public interface TelemetryEvent {

    UUID getDeviceId();
    Instant getLastUpdated();
}
