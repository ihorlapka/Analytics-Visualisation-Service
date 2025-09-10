package com.iot.devices.management.analytics_visualisation_service.metrics;

public interface KpiMetricLogger {

    void recordCacheLatency(long timeMs);
    void recordKeysSize(long keysSize);
    void incCacheMisses();

}
