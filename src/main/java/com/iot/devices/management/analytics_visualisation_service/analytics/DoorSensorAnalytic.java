package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.DoorSensorEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Getter
@Component
@RequiredArgsConstructor(staticName = "of")
public class DoorSensorAnalytic implements Analytic<DoorSensorEvent> {

    @Nullable private final Integer openCount;
    @Nullable private final Boolean temperAlert;


    @Override
    public Analytic<DoorSensorEvent> calculate(List<DoorSensorEvent> events) {
        return events.stream()
                .map(event -> DoorSensorAnalytic.of(increaseIfOpened(event), event.getTamperAlert()))
                .reduce((a, b) -> DoorSensorAnalytic.of(
                        calculate(a.getOpenCount(), b.getOpenCount(), Integer::sum),
                        isTemperAlert(a.getTemperAlert(), b.getTemperAlert())))
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Integer increaseIfOpened(DoorSensorEvent event) {
        if (event.getLastOpened() == null || event.getLastUpdated() == null) {
            return null;
        }
        return Objects.equals(event.getLastOpened(), event.getLastUpdated()) ? 1 : 0;
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
