package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.Range;
import com.iot.devices.management.analytics_visualisation_service.analytics.model.EnergyMeterAnalytic;
import com.iot.devices.management.analytics_visualisation_service.dto.EnergyMeterDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import static com.iot.devices.management.analytics_visualisation_service.util.OptionalUtils.ifAllPresentGet;

@Getter
@Component
@RequiredArgsConstructor(staticName = "of")
public class EnergyMeterAnalyticProvider implements AnalyticProvider<EnergyMeterDto, EnergyMeterAnalytic> {

    @Override
    public EnergyMeterAnalytic calculate(List<EnergyMeterDto> events) {
        return ifAllPresentGet(
                calculate(events, this::avg),
                calculate(events, this::max),
                calculate(events, this::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<EnergyMeterDto> events, BinaryOperator<Float> accumulator) {
        final List<Range<Instant>> onlineTimeRanges = getOnlineTimeRanges(events);
        return events.stream()
                .filter(event -> isOnline(event, onlineTimeRanges))
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
                min.getVoltage(), min.getCurrent(), min.getPower(), min.getEnergyConsumed()
        );
    }

    @Value(staticConstructor = "of")
    static class AnalyticParams {
        @Nullable Float voltage;
        @Nullable Float current;
        @Nullable Float power;
        @Nullable Float energyConsumed;
    }
}
