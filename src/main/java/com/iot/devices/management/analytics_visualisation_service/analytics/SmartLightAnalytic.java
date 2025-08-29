package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.Range;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartLightEvent;
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
public class SmartLightAnalytic implements Analytic<SmartLightEvent> {

    @Nullable private final Float avgBrightness;
    @Nullable private final Float avgPowerConsumption;

    @Nullable private final Float maxBrightness;
    @Nullable private final Float maxPowerConsumption;

    @Nullable private final Float minBrightness;
    @Nullable private final Float minPowerConsumption;

    @Override
    public Analytic<SmartLightEvent> calculate(List<SmartLightEvent> events) {
        return ifAllPresentGet(
                calculate(events, this::avg),
                calculate(events, this::max),
                calculate(events, this::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<SmartLightEvent> events, BinaryOperator<Float> accumulator) {
        final List<Range<Instant>> onlineTimeRanges = getOnlineTimeRanges(events);
        return events.stream()
                .filter(event -> isOnline(event, onlineTimeRanges))
                .map(event -> AnalyticParams.of(intToFloat(event.getBrightness()), event.getPowerConsumption()))
                .reduce(accumulate(accumulator));
    }

    private BinaryOperator<AnalyticParams> accumulate(BinaryOperator<Float> accumulator) {
        return (p1, p2) -> AnalyticParams.of(
                accumulator.apply(p1.getBrightness(), p2.getBrightness()),
                accumulator.apply(p1.getPowerConsumption(), p2.getPowerConsumption()));
    }

    private SmartLightAnalytic combineAnalytic(AnalyticParams avg, AnalyticParams max, AnalyticParams min) {
        return SmartLightAnalytic.of(
                avg.getBrightness(), avg.getPowerConsumption(),
                max.getBrightness(), max.getPowerConsumption(),
                min.getBrightness(), min.getPowerConsumption()
        );
    }

    @Value(staticConstructor = "of")
    static class AnalyticParams {
        @Nullable Float brightness;
        @Nullable Float powerConsumption;
    }
}
