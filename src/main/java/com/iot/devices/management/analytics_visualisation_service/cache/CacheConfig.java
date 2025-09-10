package com.iot.devices.management.analytics_visualisation_service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iot.devices.management.analytics_visualisation_service.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String PROPERTIES_PREFIX = "cache";
    public static final String DOOR_SENSOR_CACHE = "DoorSensorCache";
    public static final String ENERGY_METER_CACHE = "EnergyMeterCache";
    public static final String SMART_LIGHT_CACHE = "SmartLightCache";
    public static final String SMART_PLUG_CACHE = "SmartPlugCache";
    public static final String SOIL_MOISTURE_SENSOR_CACHE = "SoilMoistureSensorCache";
    public static final String TEMPERATURE_SENSOR_CACHE = "TemperatureSensorCache";
    public static final String THERMOSTAT_CACHE = "ThermostatCache";

//    @Bean
    //TODO: remove?
    public RedisCacheManager cacheManager(LettuceConnectionFactory factory, ObjectMapper objectMapper,
                                          @Value("${" + PROPERTIES_PREFIX + ".expiration.time.min}") int expirationTimeMin) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

        RedisCacheConfiguration doorSensorConfig = getRedisCacheConfig(objectMapper, expirationTimeMin, DoorSensorDto.class);
        RedisCacheConfiguration energyMeterConfig = getRedisCacheConfig(objectMapper, expirationTimeMin, EnergyMeterDto.class);
        RedisCacheConfiguration smartLightConfig = getRedisCacheConfig(objectMapper, expirationTimeMin, SmartLightDto.class);
        RedisCacheConfiguration smartPlugConfig = getRedisCacheConfig(objectMapper, expirationTimeMin, SmartPlugDto.class);
        RedisCacheConfiguration soilMoistureSensorConfig = getRedisCacheConfig(objectMapper, expirationTimeMin, SoilMoistureSensorDto.class);
        RedisCacheConfiguration temperatureSensorConfig = getRedisCacheConfig(objectMapper, expirationTimeMin, TemperatureSensorDto.class);
        RedisCacheConfiguration thermostatConfig = getRedisCacheConfig(objectMapper, expirationTimeMin, ThermostatDto.class);

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(DOOR_SENSOR_CACHE, doorSensorConfig);
        cacheConfigurations.put(ENERGY_METER_CACHE, energyMeterConfig);
        cacheConfigurations.put(SMART_LIGHT_CACHE, smartLightConfig);
        cacheConfigurations.put(SMART_PLUG_CACHE, smartPlugConfig);
        cacheConfigurations.put(SOIL_MOISTURE_SENSOR_CACHE, soilMoistureSensorConfig);
        cacheConfigurations.put(TEMPERATURE_SENSOR_CACHE, temperatureSensorConfig);
        cacheConfigurations.put(THERMOSTAT_CACHE, thermostatConfig);

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private <T extends TelemetryDto> RedisCacheConfiguration getRedisCacheConfig(ObjectMapper objectMapper, int expirationTimeMin, Class<T> clazz) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(expirationTimeMin))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, clazz)));
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
