package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.Range;
import com.iot.devices.management.analytics_visualisation_service.analytics.model.TemperatureSensorAnalytic;
import com.iot.devices.management.analytics_visualisation_service.dto.TemperatureSensorDto;
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
public class TemperatureSensorAnalyticProvider implements AnalyticProvider<TemperatureSensorDto, TemperatureSensorAnalytic> {

    @Override
    public TemperatureSensorAnalytic calculate(List<TemperatureSensorDto> events) {
        return ifAllPresentGet(
                calculate(events, this::avg),
                calculate(events, this::max),
                calculate(events, this::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<TemperatureSensorDto> events, BinaryOperator<Float> accumulator) {
        final List<Range<Instant>> onlineTimeRanges = getOnlineTimeRanges(events);
        return events.stream()
                .filter(event -> isOnline(event, onlineTimeRanges))
                .map(event -> AnalyticParams.of(event.getTemperature(), event.getHumidity(), event.getPressure()))
                .reduce(accumulate(accumulator));
    }

    private BinaryOperator<AnalyticParams> accumulate(BinaryOperator<Float> accumulator) {
        return (p1, p2) -> AnalyticParams.of(
                accumulator.apply(p1.getTemperature(), p2.getTemperature()),
                accumulator.apply(p1.getHumidity(), p2.getHumidity()),
                accumulator.apply(p1.getPressure(), p2.getPressure()));
    }

    private TemperatureSensorAnalytic combineAnalytic(AnalyticParams avg, AnalyticParams max, AnalyticParams min) {
        return TemperatureSensorAnalytic.of(
                avg.getTemperature(), avg.getHumidity(), avg.getPressure(),
                max.getTemperature(), max.getHumidity(), max.getPressure(),
                min.getTemperature(), min.getHumidity(), min.getPressure()
        );
    }

    @Value(staticConstructor = "of")
    static class AnalyticParams {
        @Nullable Float temperature;
        @Nullable Float humidity;
        @Nullable Float pressure;
    }
}
