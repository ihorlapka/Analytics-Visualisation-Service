package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public final class SmartPlugAnalytic implements Analytic {

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
