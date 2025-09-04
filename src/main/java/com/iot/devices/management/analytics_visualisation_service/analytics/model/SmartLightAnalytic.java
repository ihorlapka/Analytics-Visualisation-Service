package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
@Schema(name = "SmartLightAnalytic", description = "Smart Light Analytic")
public final class SmartLightAnalytic implements Analytic {
    Float avgBrightness;
    Float avgPowerConsumption;

    Float maxBrightness;
    Float maxPowerConsumption;

    Float minBrightness;
    Float minPowerConsumption;
}
