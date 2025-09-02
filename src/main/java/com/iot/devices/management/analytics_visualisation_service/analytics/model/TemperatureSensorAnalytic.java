package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
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
