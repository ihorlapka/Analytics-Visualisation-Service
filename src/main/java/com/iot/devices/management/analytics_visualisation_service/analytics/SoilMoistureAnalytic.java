package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SoilMoistureSensorEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus.ONLINE;
import static com.iot.devices.management.analytics_visualisation_service.util.OptionalUtils.ifAllPresentGet;

@Getter
@Component
@RequiredArgsConstructor(staticName = "of")
public class SoilMoistureAnalytic implements Analytic<SoilMoistureSensorEvent> {

    private final Float avgMoisturePercentage;
    private final Float avgSoilTemperature;

    private final Float maxMoisturePercentage;
    private final Float maxSoilTemperature;

    private final Float minMoisturePercentage;
    private final Float minSoilTemperature;

    @Override
    public SoilMoistureAnalytic calculate(List<SoilMoistureSensorEvent> events) {
        return ifAllPresentGet(
                calculate(events, this::calculateAvg),
                calculate(events, Math::max),
                calculate(events, Math::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<SoilMoistureSensorEvent> events, BinaryOperator<Float> accumulator) {
        return events.stream()
                .filter(event -> ONLINE.equals(event.getStatus()))
                .filter(event -> event.getBatteryLevel() > 0)
                .map(event -> AnalyticParams.of(event.getMoisturePercentage(), event.getSoilTemperature()))
                .reduce(accumulate(accumulator));
    }

    private BinaryOperator<AnalyticParams> accumulate(BinaryOperator<Float> accumulator) {
        return (p1, p2) -> AnalyticParams.of(
                accumulator.apply(p1.getMoisturePercentage(), p2.getSoilTemperature()),
                accumulator.apply(p1.getMoisturePercentage(), p2.getSoilTemperature()));
    }

    private SoilMoistureAnalytic combineAnalytic(AnalyticParams avg, AnalyticParams max, AnalyticParams min) {
        return SoilMoistureAnalytic.of(
                avg.getMoisturePercentage(), avg.getSoilTemperature(),
                max.getMoisturePercentage(), max.getSoilTemperature(),
                min.getMoisturePercentage(), min.getSoilTemperature()
        );
    }

    @Value(staticConstructor = "of")
    static class AnalyticParams {
        Float moisturePercentage;
        Float soilTemperature;
    }
}
