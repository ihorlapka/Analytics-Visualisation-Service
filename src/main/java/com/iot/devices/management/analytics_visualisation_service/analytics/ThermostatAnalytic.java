package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.Range;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.ThermostatEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import static com.iot.devices.management.analytics_visualisation_service.util.OptionalUtils.ifAllPresentGet;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ThermostatAnalytic implements Analytic<ThermostatEvent> {

    @Nullable private final Float avgCurrentTemperature;
    @Nullable private final Float avgTargetTemperature;
    @Nullable private final Float avgHumidity;

    @Nullable private final Float maxCurrentTemperature;
    @Nullable private final Float maxTargetTemperature;
    @Nullable private final Float maxHumidity;

    @Nullable private final Float minCurrentTemperature;
    @Nullable private final Float minTargetTemperature;
    @Nullable private final Float minHumidity;

    @Override
    public ThermostatAnalytic calculate(List<ThermostatEvent> events) {
        return ifAllPresentGet(
                calculate(events, this::avg),
                calculate(events, this::max),
                calculate(events, this::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<ThermostatEvent> events, BinaryOperator<Float> accumulator) {
        final List<Range<Instant>> onlineTimeRanges = getOnlineTimeRanges(events);
        return events.stream()
                .filter(event -> isOnline(event, onlineTimeRanges))
                .map(event -> AnalyticParams.of(event.getCurrentTemperature(), event.getTargetTemperature(), event.getHumidity()))
                .reduce(accumulate(accumulator));
    }

    private BinaryOperator<AnalyticParams> accumulate(BinaryOperator<Float> accumulator) {
        return (p1, p2) -> AnalyticParams.of(
                accumulator.apply(p1.getCurrentTemperature(), p2.getCurrentTemperature()),
                accumulator.apply(p1.getTargetTemperature(), p2.getTargetTemperature()),
                accumulator.apply(p1.getHumidity(), p2.getHumidity()));
    }

    private ThermostatAnalytic combineAnalytic(AnalyticParams avg, AnalyticParams max, AnalyticParams min) {
        return ThermostatAnalytic.of(
                avg.getCurrentTemperature(), avg.getTargetTemperature(), avg.getHumidity(),
                max.getCurrentTemperature(), max.getTargetTemperature(), max.getHumidity(),
                min.getCurrentTemperature(), min.getTargetTemperature(), min.getHumidity()
        );
    }

    @Value(staticConstructor = "of")
    static class AnalyticParams {
        @Nullable Float currentTemperature;
        @Nullable Float targetTemperature;
        @Nullable Float humidity;
    }
}
