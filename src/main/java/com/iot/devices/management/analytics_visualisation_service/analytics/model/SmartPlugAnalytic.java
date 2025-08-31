package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class SmartPlugAnalytic implements Analytic {

    Float avgVoltage;
    Float avgCurrent;
    Float avgPowerUsage;

    Float maxVoltage;
    Float maxCurrent;
    Float maxPowerUsage;

    Float minVoltage;
    Float minCurrent;
    Float minPowerUsage;
}
