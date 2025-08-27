package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.EnergyMeterEvent;
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
public class EnergyMeterAnalytic implements Analytic<EnergyMeterEvent> {

    private final Float avgVoltage;
    private final Float avgCurrent;
    private final Float avgPower;
    private final Float avgEnergyConsumed;

    private final Float maxVoltage;
    private final Float maxCurrent;
    private final Float maxPower;
    private final Float maxEnergyConsumed;

    private final Float minVoltage;
    private final Float minCurrent;
    private final Float minPower;
    private final Float minEnergyConsumed;


    @Override
    public EnergyMeterAnalytic calculate(List<EnergyMeterEvent> events) {
        return ifAllPresentGet(
                calculate(events, this::calculateAvg),
                calculate(events, Math::max),
                calculate(events, Math::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<EnergyMeterEvent> events, BinaryOperator<Float> accumulator) {
        return events.stream()
                .filter(event -> ONLINE.equals(event.getStatus()))
                .map(event -> AnalyticParams.of(event.getVoltage(), event.getCurrent(),
                        event.getPower(), event.getEnergyConsumed()))
                .reduce(accumulate(accumulator));
    }

    private BinaryOperator<AnalyticParams> accumulate(BinaryOperator<Float> accumulator) {
        return (p1, p2) -> AnalyticParams.of(
                accumulator.apply(p1.getVoltage(), p2.getVoltage()),
                accumulator.apply(p1.getCurrent(), p2.getCurrent()),
                accumulator.apply(p1.getPower(), p2.getPower()),
                accumulator.apply(p1.getEnergyConsumed(), p2.getEnergyConsumed())
        );
    }

    private EnergyMeterAnalytic combineAnalytic(AnalyticParams avg, AnalyticParams max, AnalyticParams min) {
        return EnergyMeterAnalytic.of(
                avg.getVoltage(), avg.getCurrent(), avg.getPower(), avg.getEnergyConsumed(),
                max.getVoltage(), max.getCurrent(), max.getPower(), max.getEnergyConsumed(),
                min.getVoltage(), min.getCurrent(), min.getPower(), max.getEnergyConsumed()
        );
    }

    @Value(staticConstructor = "of")
    static class AnalyticParams {
        Float voltage;
        Float current;
        Float power;
        Float energyConsumed;
    }
}
