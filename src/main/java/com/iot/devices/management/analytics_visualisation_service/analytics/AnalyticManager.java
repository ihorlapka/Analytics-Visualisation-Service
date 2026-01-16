package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.analytics.model.Analytic;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

@Component
public class AnalyticManager implements AnalyticRegistry {

    private final Map<DeviceType, AnalyticProvider<? extends TelemetryDto, ? extends Analytic>> analyticByType;


    public AnalyticManager(List<AnalyticProvider<? extends TelemetryDto, ? extends Analytic>> analytics) {
        this.analyticByType = analytics.stream().collect(toUnmodifiableMap(AnalyticProvider::getDeviceType, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TelemetryDto, U extends Analytic> AnalyticProvider<T, U> getProvider(DeviceType type) {
        final AnalyticProvider<T, U> provider = (AnalyticProvider<T, U>) analyticByType.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported device type: " + type);
        }
        return provider;
    }
}
