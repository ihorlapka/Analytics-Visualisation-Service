package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.Range;
import com.iot.devices.management.analytics_visualisation_service.analytics.model.SmartPlugAnalytic;
import com.iot.devices.management.analytics_visualisation_service.dto.SmartPlugDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.SMART_PLUG;
import static com.iot.devices.management.analytics_visualisation_service.util.OptionalUtils.ifAllPresentGet;

@Getter
@Component
@RequiredArgsConstructor(staticName = "of")
public class SmartPlugAnalyticProvider implements AnalyticProvider<SmartPlugDto, SmartPlugAnalytic> {

    @Override
    public DeviceType getDeviceType() {
        return SMART_PLUG;
    }

    @Override
    public SmartPlugAnalytic calculate(List<SmartPlugDto> events) {
        return ifAllPresentGet(
                calculate(events, this::avg),
                calculate(events, this::max),
                calculate(events, this::min),
                this::combineAnalytic)
                .orElseThrow(() -> new IllegalStateException("Analytic calculation failed"));
    }

    private Optional<AnalyticParams> calculate(List<SmartPlugDto> events, BinaryOperator<Float> accumulator) {
        final List<Range<Instant>> onlineTimeRanges = getOnlineTimeRanges(events);
        return events.stream()
                .filter(event -> isOnline(event, onlineTimeRanges))
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
        @Nullable Float voltage;
        @Nullable Float current;
        @Nullable Float powerUsage;
    }
}
