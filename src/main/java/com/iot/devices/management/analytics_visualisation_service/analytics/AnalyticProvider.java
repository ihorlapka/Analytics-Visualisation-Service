package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.Range;
import com.iot.devices.management.analytics_visualisation_service.analytics.model.Analytic;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus.*;
import static java.math.RoundingMode.HALF_UP;

public interface AnalyticProvider<T extends TelemetryDto, U extends Analytic> {

    DeviceType getDeviceType();
    U calculate(List<T> events);

    default Float avg(Float param1, Float param2) {
        return calculateValue(param1, param2, (p1, p2) ->
                BigDecimal.valueOf(param1 + param2)
                        .divide(BigDecimal.valueOf(2), 3, HALF_UP)
                        .floatValue());
    }

    default Float max(Float param1, Float param2) {
        return calculateValue(param1, param2, (p1, p2) -> Math.max(param1, param2));
    }

    default Float min(Float param1, Float param2) {
        return calculateValue(param1, param2, (p1, p2) -> Math.min(param1, param2));
    }

    default<N extends Number> N calculateValue(N param1, N param2, BinaryOperator<N> operation) {
        if (param1 == null && param2 == null) {
            return null;
        }
        if (param2 == null) {
            return param1;
        }
        if (param1 == null) {
            return param2;
        }
        return operation.apply(param1, param2);
    }

    default Float intToFloat(@Nullable Integer brightness) {
        return (brightness == null) ? null : (float) brightness;
    }

    default <E extends TelemetryDto> List<Range<Instant>> getOnlineTimeRanges(List<E> events) {
        final List<E> sorted = events.stream().sorted(Comparator.comparingLong(event -> event.getLastUpdated().toEpochMilli())).toList();
        final Set<DeviceStatus> notNeededStatuses = Set.of(OFFLINE, MAINTENANCE, ERROR);
        final List<Instant> startTimes = new ArrayList<>();
        final List<Instant> endTimes = new ArrayList<>();
        boolean isRangeStart = true;
        for (E event : sorted) {
            if (notNeededStatuses.contains(event.getStatus()) && !isRangeStart) {
                endTimes.add(event.getLastUpdated());
                isRangeStart = true;
            }
            else if (isRangeStart) { //online
                startTimes.add(event.getLastUpdated());
                isRangeStart = false;
            }
        }
        final int rangesSize = calculateValue(startTimes.size(), endTimes.size(), Math::max);
        final List<Range<Instant>> onlineTimeRanges = new ArrayList<>(rangesSize);
        for (int i = 0; i < rangesSize; i++) {
            if (startTimes.size() > i && endTimes.size() > i) {
                onlineTimeRanges.add(Range.closed(startTimes.get(i), endTimes.get(i)));
            } else if (startTimes.size() > i) {
                onlineTimeRanges.add(Range.atLeast(startTimes.get(i)));
            }
        }
        return onlineTimeRanges;
    }

    default <E extends TelemetryDto> boolean isOnline(E event, List<Range<Instant>> onlineTimeRanges) {
        return onlineTimeRanges.stream()
                .anyMatch(range -> range.contains(event.getLastUpdated()));
    }
}
