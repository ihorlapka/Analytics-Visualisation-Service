package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.SeverityLevel;
import lombok.*;
import org.springframework.data.annotation.Id;
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
@EqualsAndHashCode(of = {"alertId"})
@Document(collection = AlertEvent.ALERTS_COLLECTION)
@TimeSeries(
        timeField = "timestamp",
        metaField = "deviceId",
        granularity = MINUTES
)
public class AlertEvent {
    public static final String ALERTS_COLLECTION = "alerts";
    @Id
    private UUID alertId;

    private UUID deviceId;

    private UUID ruleId;

    public SeverityLevel severity;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public Instant timestamp;

    public String message;

    public Float actualValue;
}
