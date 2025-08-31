package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.Range;
import com.iot.devices.management.analytics_visualisation_service.analytics.model.SoilMoistureAnalytic;
import com.iot.devices.management.analytics_visualisation_service.dto.SoilMoistureSensorDto;
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
public class SoilMoistureAnalyticProvider implements AnalyticProvider<SoilMoistureSensorDto, SoilMoistureAnalytic> {

    @Override
    public SoilMoistureAnalytic calculate(List<SoilMoistureSensorDto> events) {
        return ifAllPresentGet(
                calculate(events, this::avg),
                calculate(events, Math::max),
                calculate(events, Math::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<SoilMoistureSensorDto> events, BinaryOperator<Float> accumulator) {
        final List<Range<Instant>> onlineTimeRanges = getOnlineTimeRanges(events);
        return events.stream()
                .filter(event -> isOnline(event, onlineTimeRanges))
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
        @Nullable Float moisturePercentage;
        @Nullable Float soilTemperature;
    }
}
