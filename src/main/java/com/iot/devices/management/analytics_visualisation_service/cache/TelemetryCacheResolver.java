package com.iot.devices.management.analytics_visualisation_service.cache;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.iot.devices.management.analytics_visualisation_service.cache.CacheConfig.*;

@Component
@RequiredArgsConstructor
public class TelemetryCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;

    @NonNull
    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Object[] args = context.getArgs();
        if (args.length > 1 && args[1] instanceof DeviceType deviceType) {
            Cache cache = cacheManager.getCache(deviceType.getCacheName());
            if (cache != null) {
                return Collections.singleton(cache);
            } else {
                throw new IllegalStateException("Cache for deviceType: " + deviceType + " is not found");
            }
        }
        throw new IllegalStateException("Failed to resolve cache with args:" + Arrays.toString(args));
    }
}
