package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.DoorSensorEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus.ONLINE;

@Getter
@Component
@RequiredArgsConstructor(staticName = "of")
public class DoorSensorAnalytic implements Analytic<DoorSensorEvent> {

    private final int openCount;
    private final boolean temperAlert;


    @Override
    public Analytic<DoorSensorEvent> calculate(List<DoorSensorEvent> events) {
        return events.stream()
                .filter(event -> ONLINE.equals(event.getStatus()))
                .filter(event -> event.getBatteryLevel() > 0)
                .map(event -> DoorSensorAnalytic.of(increaseIfOpened(event), event.getTamperAlert()))
                .reduce((a, b) -> DoorSensorAnalytic.of(
                        a.getOpenCount() + b.getOpenCount(),
                        a.isTemperAlert() || b.isTemperAlert()))
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private int increaseIfOpened(DoorSensorEvent event) {
        return Objects.equals(event.getLastOpened(), event.getLastUpdated()) ? 1 : 0;
    }
}
