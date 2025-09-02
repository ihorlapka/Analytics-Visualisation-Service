package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public final class EnergyMeterAnalytic implements Analytic {
    Float avgVoltage;
    Float avgCurrent;
    Float avgPower;
    Float avgEnergyConsumed;

    Float maxVoltage;
    Float maxCurrent;
    Float maxPower;
    Float maxEnergyConsumed;

    Float minVoltage;
    Float minCurrent;
    Float minPower;
    Float minEnergyConsumed;
}
