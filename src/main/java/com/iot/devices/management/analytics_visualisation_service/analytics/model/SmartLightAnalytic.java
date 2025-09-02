package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public final class SmartLightAnalytic implements Analytic {
    Float avgBrightness;
    Float avgPowerConsumption;

    Float maxBrightness;
    Float maxPowerConsumption;

    Float minBrightness;
    Float minPowerConsumption;
}
