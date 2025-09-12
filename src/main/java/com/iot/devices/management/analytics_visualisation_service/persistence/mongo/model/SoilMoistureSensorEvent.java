package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.data.mongodb.core.timeseries.Granularity.MINUTES;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"deviceId", "lastUpdated"})
@Document(collection = SoilMoistureSensorEvent.SOIL_MOISTER_SENSORS_COLLECTION)
@TimeSeries(
        timeField = "lastUpdated",
        metaField = "deviceId",
        granularity = MINUTES
)
public class SoilMoistureSensorEvent implements TelemetryEvent {

    public static final String SOIL_MOISTER_SENSORS_COLLECTION = "soil_moisture_sensors";

    private UUID deviceId;

    private Float moisturePercentage;

    private Float soilTemperature;

    private Integer batteryLevel;

    private DeviceStatus status;

    private String firmwareVersion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant lastUpdated;
}
