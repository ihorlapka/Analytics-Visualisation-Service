package com.iot.devices.management.analytics_visualisation_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class PrometheusKpiMetricLogger implements KpiMetricLogger {

    private final AtomicLong keysSize = new AtomicLong(0);

    private final DistributionSummary cacheLatency;
    private final Counter cacheMisses;


    public PrometheusKpiMetricLogger(MeterRegistry meterRegistry) {
        this.cacheLatency = DistributionSummary.builder("avs_cache_receiving_time")
                .description("The time during which data was retrieved from cache")
                .publishPercentiles(0.5, 0.9, 0.99)
                .register(meterRegistry);

        this.cacheMisses = Counter.builder("avs_cache_miss")
                .description("Count values which were not retrieved from cache")
                .register(meterRegistry);

        Gauge.builder("avs_redis_keys_size_gauge", keysSize, AtomicLong::get)
                .description("The number of entries in redis")
                .register(meterRegistry);
    }

    @Override
    public void recordCacheLatency(long timeMs) {
        cacheLatency.record(timeMs);
    }

    @Override
    public void recordKeysSize(long size) {
        keysSize.set(size);
    }

    @Override
    public void incCacheMisses() {
        cacheMisses.increment();
    }
}
