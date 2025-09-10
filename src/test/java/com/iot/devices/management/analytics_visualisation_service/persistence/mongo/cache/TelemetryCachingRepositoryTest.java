package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iot.devices.management.analytics_visualisation_service.metrics.KpiMetricLogger;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo.*;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services.TelemetryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@DataMongoTest
@ContextConfiguration( classes = {
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

    private static final String DATABASE_NAME = "my-db";

    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.23"))
            .withEnv("MONGO_INITDB_DATABASE", DATABASE_NAME);

    static GenericContainer<?> redisContainer =
            new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getConnectionString);
        registry.add("spring.data.mongodb.database", () -> DATABASE_NAME);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }

    @MockitoBean
    KpiMetricLogger kpiMetricLogger;

    @BeforeAll
    static void start() {
        mongoDBContainer.start();
        redisContainer.start();
    }

    @AfterAll
    static void close() {
        mongoDBContainer.close();
        redisContainer.close();
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void test() {

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