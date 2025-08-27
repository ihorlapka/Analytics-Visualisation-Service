package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.TelemetryEvent;

import java.math.BigDecimal;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

public interface Analytic<T extends TelemetryEvent> {

    Analytic<T> calculate(List<T> events);

    default float calculateAvg(float param1, float param2) {
        return BigDecimal.valueOf(param1 + param2)
                .divide(BigDecimal.valueOf(2), 3, HALF_UP)
                .floatValue();
    }
}
