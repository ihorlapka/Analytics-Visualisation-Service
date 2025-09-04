package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
@Schema(name = "EnergyMeterAnalytic", description = "Energy Meter Analytic")
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
