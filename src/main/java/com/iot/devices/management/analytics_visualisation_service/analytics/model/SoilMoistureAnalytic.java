package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class SoilMoistureAnalytic implements Analytic {

    Float avgMoisturePercentage;
    Float avgSoilTemperature;

    Float maxMoisturePercentage;
    Float maxSoilTemperature;

    Float minMoisturePercentage;
    Float minSoilTemperature;
}
