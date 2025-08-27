package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
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
public class SmartPlugAnalytic implements Analytic<SmartPlugEvent> {

    private final Float avgVoltage;
    private final Float avgCurrent;
    private final Float avgPowerUsage;

    private final Float maxVoltage;
    private final Float maxCurrent;
    private final Float maxPowerUsage;

    private final Float minVoltage;
    private final Float minCurrent;
    private final Float minPowerUsage;


    @Override
    public SmartPlugAnalytic calculate(List<SmartPlugEvent> events) {
        return ifAllPresentGet(
                calculate(events, this::calculateAvg),
                calculate(events, Math::max),
                calculate(events, Math::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<SmartPlugEvent> events, BinaryOperator<Float> accumulator) {
        return events.stream()
                .filter(event -> ONLINE.equals(event.getStatus()))
                .filter(SmartPlugEvent::getIsOn)
                .map(event -> AnalyticParams.of(event.getVoltage(), event.getCurrent(), event.getPowerUsage()))
                .reduce(accumulate(accumulator));
    }

    private BinaryOperator<AnalyticParams> accumulate(BinaryOperator<Float> accumulator) {
        return (p1, p2) -> AnalyticParams.of(
                accumulator.apply(p1.getVoltage(), p2.getVoltage()),
                accumulator.apply(p1.getCurrent(), p2.getCurrent()),
                accumulator.apply(p1.getPowerUsage(), p2.getPowerUsage()));
    }

    private SmartPlugAnalytic combineAnalytic(AnalyticParams avg, AnalyticParams max, AnalyticParams min) {
        return SmartPlugAnalytic.of(
                avg.getVoltage(), avg.getCurrent(), avg.getPowerUsage(),
                max.getVoltage(), max.getCurrent(), max.getPowerUsage(),
                min.getVoltage(), min.getCurrent(), min.getPowerUsage()
        );
    }

    @Value(staticConstructor = "of")
    static class AnalyticParams {
        Float voltage;
        Float current;
        Float powerUsage;
    }
}
