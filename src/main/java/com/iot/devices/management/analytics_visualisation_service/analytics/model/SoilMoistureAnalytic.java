package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public final class SoilMoistureAnalytic implements Analytic {

    Float avgMoisturePercentage;
    Float avgSoilTemperature;

    Float maxMoisturePercentage;
    Float maxSoilTemperature;

    Float minMoisturePercentage;
    Float minSoilTemperature;
}
