package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
@Schema(name = "ThermostatAnalytic", description = "Thermostat Analytic")
public final class ThermostatAnalytic implements Analytic {

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
