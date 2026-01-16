package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.SMART_LIGHT;
import static org.springframework.data.mongodb.core.timeseries.Granularity.MINUTES;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"deviceId", "lastUpdated"})
@Document(collection = SmartLightEvent.SMART_LIGHTS_COLLECTION)
@TimeSeries(
        timeField = "lastUpdated",
        metaField = "deviceId",
        granularity = MINUTES
)
public class SmartLightEvent implements TelemetryEvent{

    public static final String SMART_LIGHTS_COLLECTION = "smart_lights";
    @Id
    private UUID deviceId;

    private Boolean isOn;

    private Integer brightness;

    private String colour;

    private String mode;

    private Float powerConsumption;

    private DeviceStatus status;

    private String firmwareVersion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant lastUpdated;

    @Override
    public DeviceType getDeviceType() {
        return SMART_LIGHT;
    }
}
