package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.ThermostatMode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.THERMOSTAT;
import static org.springframework.data.mongodb.core.timeseries.Granularity.MINUTES;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"deviceId", "lastUpdated"})
@Document(collection = ThermostatEvent.THERMOSTATS_COLLECTION)
@TimeSeries(
        timeField = "lastUpdated",
        metaField = "deviceId",
        granularity = MINUTES
)
public class ThermostatEvent implements TelemetryEvent {

    public static final String THERMOSTATS_COLLECTION = "thermostats";
    @Id
    private UUID deviceId;

    private Float currentTemperature;

    private Float targetTemperature;

    private Float humidity;

    private ThermostatMode mode;

    private DeviceStatus status;

    private String firmwareVersion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant lastUpdated;

    @Override
    public DeviceType getDeviceType() {
        return THERMOSTAT;
    }
}
