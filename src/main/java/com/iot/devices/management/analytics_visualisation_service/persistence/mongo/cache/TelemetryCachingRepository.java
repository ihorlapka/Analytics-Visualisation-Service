package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.metrics.KpiMetricLogger;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services.TelemetryService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RListReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.cache.CacheConfig.PROPERTIES_PREFIX;
import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofMinutes;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.emptyList;

@Slf4j
@Component
public class TelemetryCachingRepository {

    public static final String CACHE_SIMPLE_KEY = "Cache::SimpleKey";

    private final TelemetryService telemetryService;
    private final RedissonReactiveClient redissonReactiveClient;
    private final ObjectMapper objectMapper;
    private final KpiMetricLogger kpiMetricLogger;
    private final int expirationTimeMin;

    public TelemetryCachingRepository(TelemetryService telemetryService, RedissonReactiveClient redissonReactiveClient,
                                      ObjectMapper objectMapper, KpiMetricLogger kpiMetricLogger,
                                      @Value("${" + PROPERTIES_PREFIX + ".expiration.time.min}") int expirationTimeMin) {
        this.telemetryService = telemetryService;
        this.redissonReactiveClient = redissonReactiveClient;
        this.objectMapper = objectMapper;
        this.kpiMetricLogger = kpiMetricLogger;
        this.expirationTimeMin = expirationTimeMin;
    }

    public Mono<List<TelemetryDto>> getFromCacheOrDb(UUID deviceId, Instant from, Instant to, DeviceType deviceType) {
        if (!isAllowedForCaching(from, to)) {
            return telemetryService.findByDeviceIdAndLastUpdatedBetween(deviceId, from, to, deviceType);
        }
        final String key = createKey(deviceId, from, to, deviceType);
        final long startTime = currentTimeMillis();
        final RListReactive<TelemetryDto> cache = redissonReactiveClient.getList(key, new TypedJsonJacksonCodec(deviceType.getDtoClass(), objectMapper));
        return cache.readAll()
                .filter(cacheList -> !cacheList.isEmpty())
                .flatMap(cacheList -> {
                    kpiMetricLogger.recordCacheLatency(currentTimeMillis() - startTime);
                    log.info("{} elements were found in cache by key: {}", cacheList.size(), key);
                    return Mono.just(cacheList);
                })
                .switchIfEmpty(loadFromDbAndStoreToCache(deviceId, from, to, deviceType, cache, key));
    }

    private Mono<List<TelemetryDto>> loadFromDbAndStoreToCache(UUID deviceId, Instant from, Instant to, DeviceType deviceType,
                                                               RListReactive<TelemetryDto> cache, String key) {
        return telemetryService.findByDeviceIdAndLastUpdatedBetween(deviceId, from, to, deviceType)
                .doOnSuccess(dbList -> kpiMetricLogger.incCacheMisses())
                .flatMap(dbList -> {
                    if (!dbList.isEmpty()) {
                        return cache.addAll(dbList)
                                .then(cache.expire(ofMinutes(expirationTimeMin)))
                                .doOnSuccess(ignored -> log.info("{} elements were added to cache by key: {}", dbList.size(), key))
                                .thenReturn(dbList);
                    }
                    return Mono.just(emptyList());
                });
    }

    private boolean isAllowedForCaching(Instant from, Instant to) {
        return from.truncatedTo(MINUTES).equals(from) && to.truncatedTo(MINUTES).equals(to);
    }

    private String createKey(UUID deviceId, Instant from, Instant to, DeviceType deviceType) {
        return deviceType.getId() + CACHE_SIMPLE_KEY + " [" + deviceId + ", " + from + ", " + to + "]";
    }
}
