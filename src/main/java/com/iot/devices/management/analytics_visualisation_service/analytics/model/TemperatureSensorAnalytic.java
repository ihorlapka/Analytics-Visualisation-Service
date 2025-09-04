package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
@Schema(name = "TemperatureSensorAnalytic", description = "Temperature Sensor Analytic")
public final class TemperatureSensorAnalytic implements Analytic {

    Float avgTemperature;
    Float avgHumidity;
    Float avgPressure;

    Float maxTemperature;
    Float maxHumidity;
    Float maxPressure;

    Float minTemperature;
    Float minHumidity;
    Float minPressure;
}
