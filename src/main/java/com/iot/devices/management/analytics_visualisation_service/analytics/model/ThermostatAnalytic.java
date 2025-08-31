package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class ThermostatAnalytic implements Analytic {

    Float avgCurrentTemperature;
    Float avgTargetTemperature;
    Float avgHumidity;

    Float maxCurrentTemperature;
    Float maxTargetTemperature;
    Float maxHumidity;

    Float minCurrentTemperature;
    Float minTargetTemperature;
    Float minHumidity;
}
