package com.iot.devices.management.analytics_visualisation_service.cache;

import com.iot.devices.management.analytics_visualisation_service.metrics.KpiMetricLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.options.KeysScanParams;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache.TelemetryCachingRepository.CACHE_SIMPLE_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSizeMetricsUpdater {

    private final RedissonReactiveClient redissonReactiveClient;
    private final KpiMetricLogger kpiMetricLogger;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void recordCacheSize() {
        redissonReactiveClient.getKeys()
                .getKeys(new KeysScanParams().pattern("*" + CACHE_SIMPLE_KEY + "*"))
                .count()
                .filter(Objects::nonNull)
                .doOnSuccess(kpiMetricLogger::recordKeysSize)
                .doOnError(error -> log.error("Failed to update cache key count metric", error))
                .subscribe();
    }
}
