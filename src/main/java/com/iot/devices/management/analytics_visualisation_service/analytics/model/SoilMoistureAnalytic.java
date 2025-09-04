package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
@Schema(name = "SoilMoistureAnalytic", description = "Soil Moisture Analytic")
public final class SoilMoistureAnalytic implements Analytic {

    Float avgMoisturePercentage;
    Float avgSoilTemperature;

    Float maxMoisturePercentage;
    Float maxSoilTemperature;

    Float minMoisturePercentage;
    Float minSoilTemperature;
}
