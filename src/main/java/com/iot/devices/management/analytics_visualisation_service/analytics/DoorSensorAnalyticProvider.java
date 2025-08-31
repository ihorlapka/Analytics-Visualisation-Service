package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.analytics.model.DoorSensorAnalytic;
import com.iot.devices.management.analytics_visualisation_service.dto.DoorSensorDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Getter
@Component
@RequiredArgsConstructor(staticName = "of")
public class DoorSensorAnalyticProvider implements AnalyticProvider<DoorSensorDto, DoorSensorAnalytic> {

    @Override
    public DoorSensorAnalytic calculate(List<DoorSensorDto> events) {
        return events.stream()
                .map(event -> DoorSensorAnalytic.of(increaseIfOpened(event), event.getIsTamperAlert()))
                .reduce((a, b) -> DoorSensorAnalytic.of(
                        calculateValue(a.getOpenCount(), b.getOpenCount(), Integer::sum),
                        isTemperAlert(a.getTemperAlert(), b.getTemperAlert())))
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Integer increaseIfOpened(DoorSensorDto event) {
        if (event.getIsLastOpened() == null) {
            return null;
        }
        return Objects.equals(event.getIsLastOpened(), event.getLastUpdated()) ? 1 : 0;
    }

    private Boolean isTemperAlert(Boolean a, Boolean b) {
        if (a == null && b == null) {
            return null;
        }
        if (b == null) {
            return a;
        }
        if (a == null) {
            return b;
        }
        return a || b;
    }
}
