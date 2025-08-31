package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.analytics.model.Analytic;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;

public interface AnalyticRegistry {

    <T extends TelemetryDto, U extends Analytic> AnalyticProvider<T, U> getProvider(DeviceType type);

}
