package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.mapping.EventToDtoMapper;
import com.iot.devices.management.analytics_visualisation_service.metrics.KpiMetricLogger;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.ThermostatMode;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.ThermostatEvent;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo.*;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services.TelemetryService;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesGranularity;
import com.mongodb.client.model.TimeSeriesOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.THERMOSTAT;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static jodd.util.ThreadUtil.sleep;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@DataMongoTest
@ContextConfiguration(classes = {
        TelemetryService.class,
        DoorSensorRepository.class,
        EnergyMeterRepository.class,
        SmartLightRepository.class,
        SmartPlugRepository.class,
        SoilMoistureSensorRepository.class,
        TemperatureSensorRepository.class,
        ThermostatRepository.class,
        TelemetryCachingRepositoryTest.TestRepositoriesConfig.class,
        TelemetryCachingRepository.class
})
@TestPropertySource("classpath:application-test.yaml")
@Testcontainers
class TelemetryCachingRepositoryTest {

    private static final String DATABASE_NAME = "telemetry-events";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.23"))
            .withEnv("MONGO_INITDB_DATABASE", DATABASE_NAME);

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getConnectionString);
        registry.add("spring.data.mongodb.database", () -> DATABASE_NAME);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }

    static Instant FROM = LocalDateTime.parse("2025-08-17T13:19:00").toInstant(UTC);
    static Instant TO = LocalDateTime.parse("2025-08-17T13:20:00").toInstant(UTC);

    @MockitoBean
    KpiMetricLogger kpiMetricLogger;
    @Autowired
    TelemetryCachingRepository telemetryCachingRepository;
    @Autowired
    ThermostatRepository thermostatRepository;
    @Autowired
    ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory;

    @BeforeEach
    void createTimeSeriesCollectionIfNotExist() {
        reactiveMongoDatabaseFactory.getMongoDatabase()
                .flatMap(database -> Flux.from(database.listCollectionNames())
                        .filter(name -> name.equals(ThermostatEvent.THERMOSTATS_COLLECTION))
                        .hasElements()
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.empty();
                            }
                            return Mono.from(database.createCollection(ThermostatEvent.THERMOSTATS_COLLECTION,
                                    new CreateCollectionOptions().timeSeriesOptions(
                                            new TimeSeriesOptions("lastUpdated")
                                                    .metaField("deviceId")
                                                    .granularity(TimeSeriesGranularity.MINUTES))
                            ));
                        })
                )
                .block();
    }

    @AfterEach
    void tearDown() {
        thermostatRepository.deleteAll().block();
        reactiveMongoDatabaseFactory.getMongoDatabase()
                .map(database -> database.getCollection(ThermostatEvent.THERMOSTATS_COLLECTION).drop())
                .block();
        Mockito.verifyNoMoreInteractions(kpiMetricLogger);
    }

    @Test
    void dbAndCacheAreEmpty() {
        UUID uuid = UUID.randomUUID();
        Mono<List<TelemetryDto>> data = telemetryCachingRepository.getFromCacheOrDb(uuid, FROM, TO, THERMOSTAT);

        StepVerifier.create(data)
                .expectNext(List.of())
                .expectComplete()
                .verify();

        verify(kpiMetricLogger).incCacheMisses();
    }

    @Test
    void notAllowedForCaching() {
        UUID uuid = UUID.randomUUID();
        List<ThermostatEvent> thermostats = getThermostats(uuid);
        thermostatRepository.insert(thermostats).blockLast();

        Mono<List<TelemetryDto>> data = telemetryCachingRepository.getFromCacheOrDb(uuid, FROM, TO.plus(1, MILLIS), THERMOSTAT);

        StepVerifier.create(data)
                .expectNext(Stream.of(thermostats.get(1), thermostats.get(2), thermostats.get(3))
                        .map(EventToDtoMapper::mapToDto)
                        .toList())
                .expectComplete()
                .verify();
    }

    @Test
    void cacheIsEmptyAndDbNot() {
        UUID uuid = UUID.randomUUID();
        List<ThermostatEvent> thermostats = getThermostats(uuid);
        thermostatRepository.insert(thermostats).blockLast();

        Mono<List<TelemetryDto>> data = telemetryCachingRepository.getFromCacheOrDb(uuid, FROM, TO, THERMOSTAT);

        StepVerifier.create(data)
                .expectNext(Stream.of(thermostats.get(1), thermostats.get(2), thermostats.get(3))
                        .map(EventToDtoMapper::mapToDto)
                        .toList())
                .expectComplete()
                .verify();

        verify(kpiMetricLogger).incCacheMisses();
    }


    @Test
    void cacheIsNotEmpty() {
        UUID uuid = UUID.randomUUID();
        List<ThermostatEvent> thermostats = getThermostats(uuid);
        thermostatRepository.insert(thermostats).blockLast();

        Mono<List<TelemetryDto>> firstCall = telemetryCachingRepository.getFromCacheOrDb(uuid, FROM, TO, THERMOSTAT);
        StepVerifier.create(firstCall)
                .expectNext(Stream.of(thermostats.get(1), thermostats.get(2), thermostats.get(3))
                        .map(EventToDtoMapper::mapToDto)
                        .toList())
                .expectComplete()
                .verify();

        sleep(2000);

        Mono<List<TelemetryDto>> secondCall = telemetryCachingRepository.getFromCacheOrDb(uuid, FROM, TO, THERMOSTAT);
        StepVerifier.create(secondCall)
                .expectNext(Stream.of(thermostats.get(1), thermostats.get(2), thermostats.get(3))
                        .map(EventToDtoMapper::mapToDto)
                        .toList())
                .expectComplete()
                .verify();

        verify(kpiMetricLogger).incCacheMisses(); //first call
        verify(kpiMetricLogger).recordCacheLatency(anyLong()); //second call
    }

    private List<ThermostatEvent> getThermostats(UUID uuid) {
        ThermostatEvent event0 = ThermostatEvent.builder()
                .deviceId(uuid)
                .status(DeviceStatus.ONLINE)
                .lastUpdated(FROM.minus(5, SECONDS))
                .mode(ThermostatMode.COOL)
                .currentTemperature(22f)
                .build();

        ThermostatEvent event1 = ThermostatEvent.builder()
                .deviceId(uuid)
                .status(DeviceStatus.ONLINE)
                .lastUpdated(FROM.plus(10, SECONDS))
                .mode(ThermostatMode.HEAT)
                .currentTemperature(23f)
                .build();

        ThermostatEvent event2 = ThermostatEvent.builder()
                .deviceId(uuid)
                .status(DeviceStatus.ONLINE)
                .lastUpdated(FROM.plus(15, SECONDS))
                .currentTemperature(24f)
                .build();

        ThermostatEvent event3 = ThermostatEvent.builder()
                .deviceId(uuid)
                .status(DeviceStatus.ONLINE)
                .lastUpdated(FROM.plus(20, SECONDS))
                .currentTemperature(25f)
                .build();

        ThermostatEvent event4 = ThermostatEvent.builder()
                .deviceId(uuid)
                .status(DeviceStatus.ONLINE)
                .lastUpdated(FROM.plus(70, SECONDS))
                .currentTemperature(25f)
                .build();

        return List.of(event0, event1, event2, event3, event4);
    }


    @Configuration
    @EnableReactiveMongoRepositories(basePackages = "com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo")
    public static class TestRepositoriesConfig {

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }

        @Bean(destroyMethod = "shutdown")
        public RedissonReactiveClient redissonReactiveClient() {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + redisContainer.getHost() + ":" + redisContainer.getFirstMappedPort());
            return Redisson.create(config).reactive();
        }
    }
}