package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class SmartLightAnalytic implements Analytic {
    Float avgBrightness;
    Float avgPowerConsumption;

    Float maxBrightness;
    Float maxPowerConsumption;

    Float minBrightness;
    Float minPowerConsumption;
}
