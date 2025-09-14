package com.iot.devices.management.analytics_visualisation_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.devices.*;
import com.iot.devices.management.analytics_visualisation_service.dto.DoorSensorDto;
import com.iot.devices.management.analytics_visualisation_service.dto.SmartPlugDto;
import com.iot.devices.management.analytics_visualisation_service.dto.ThermostatDto;
import com.iot.devices.management.analytics_visualisation_service.health.HealthConfig;
import com.iot.devices.management.analytics_visualisation_service.kafka.producer.KafkaProducerProperties;
import com.iot.devices.management.analytics_visualisation_service.kafka.producer.TestKafkaProducer;
import com.iot.devices.management.analytics_visualisation_service.metrics.KpiMetricLogger;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache.TelemetryCachingRepository;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services.TelemetryService;
import com.iot.devices.management.analytics_visualisation_service.stream.TelemetryStream;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.iot.devices.DoorState.OPEN;
import static com.iot.devices.management.analytics_visualisation_service.mapping.RecordToDtoMapper.*;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.*;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(
        classes = {
                ReactiveKafkaConsumerRunner.class,
                TelemetryStream.class,
                KafkaConsumerConfig.class,
                TestKafkaProducer.class,
                KafkaProducerProperties.class,
                KafkaConsumerProperties.class,
                HealthConfig.class,
                SimpleMeterRegistry.class,
                MockClock.class,
                TelemetryCachingRepository.class,
                ObjectMapper.class
        },
        properties = "classpath:application-test.yaml")
@Testcontainers
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
public class ReactiveKafkaConsumerRunnerTest {

    @Autowired
    ReactiveKafkaConsumerRunner reactiveKafkaConsumerRunner;
    @Autowired
    TestKafkaProducer kafkaProducer;
    @MockitoSpyBean
    TelemetryStream telemetryStream;
    @MockitoBean
    TelemetryService telemetryService;
    @MockitoBean
    RedissonReactiveClient redissonReactiveClient;
    @MockitoBean
    KpiMetricLogger kpiMetricLogger;

    @Container
    @SuppressWarnings("deprecation")
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.9.0"));

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("kafka.consumer.properties.bootstrap.servers", kafkaContainer::getBootstrapServers);
        registry.add("kafka.producer.properties.bootstrap.servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeAll
    static void start() {
        kafkaContainer.start();
    }

    @AfterAll
    static void close() {
        kafkaContainer.close();
    }

    @BeforeEach
    void startConsumer() {
        reactiveKafkaConsumerRunner.consumeRecord();
    }

    @AfterEach
    void tearDown() {
        telemetryStream.purge();
        reset(telemetryStream);
        reactiveKafkaConsumerRunner.shutdown();
    }

    UUID deviceId1 = UUID.randomUUID();
    UUID deviceId2 = UUID.randomUUID();
    UUID deviceId3 = UUID.randomUUID();

    Instant nowTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    DoorSensor doorSensor = new DoorSensor(deviceId1.toString(), OPEN, 85, false,
            DeviceStatus.OFFLINE, nowTime, "1.0.2v", nowTime);

    Thermostat thermostat = new Thermostat(deviceId2.toString(), 26.6f, 24.0f, 10.0f,
            ThermostatMode.COOL, DeviceStatus.ONLINE, "2.123v", nowTime);

    SmartPlug smartPlug = new SmartPlug(deviceId3.toString(), true, 230f, 227f, 99f,
            DeviceStatus.MAINTENANCE, null, nowTime.minus(5, ChronoUnit.MINUTES));

    @Test
    void simpleTest() {
        when(telemetryStream.publish(doorSensor)).thenCallRealMethod();
        when(telemetryStream.publish(thermostat)).thenCallRealMethod();
        when(telemetryStream.publish(smartPlug)).thenCallRealMethod();

        StepVerifier.create(telemetryStream.getStream(DOOR_SENSOR, deviceId1))
                .expectNext(mapDoorSensor(doorSensor))
                .thenCancel()
                .verify();

        StepVerifier.create(telemetryStream.getStream(THERMOSTAT, deviceId2))
                .expectNext(mapThermostat(thermostat))
                .thenCancel()
                .verify();

        StepVerifier.create(telemetryStream.getStream(SMART_PLUG, deviceId3))
                .expectNext(mapSmartPlug(smartPlug))
                .thenCancel()
                .verify();

        sendMessage(kafkaProducer.sendMessage(doorSensor, deviceId1));
        sendMessage(kafkaProducer.sendMessage(thermostat, deviceId2));
        sendMessage(kafkaProducer.sendMessage(smartPlug, deviceId3));

        verify(telemetryStream, timeout(1000)).publish(doorSensor);
        verify(telemetryStream, timeout(1000)).publish(thermostat);
        verify(telemetryStream, timeout(1000)).publish(smartPlug);
    }

    @Test
    void consumerRestarts() {
        when(telemetryStream.publish(doorSensor)).thenCallRealMethod();
        when(telemetryStream.publish(thermostat))
                .thenReturn(Mono.error(new RuntimeException("Test Error: First attempt")))
                .thenReturn(Mono.error(new RuntimeException("Test Error: Second attempt")))
                .thenReturn(Mono.error(new RuntimeException("Test Error: Third attempt")))
                .thenReturn(Mono.empty());
        when(telemetryStream.publish(smartPlug)).thenCallRealMethod();

        Flux<DoorSensorDto> doorSensorStream = telemetryStream.getStream(DOOR_SENSOR, deviceId1);
        Flux<ThermostatDto> thermostatStream = telemetryStream.getStream(THERMOSTAT, deviceId2);
        Flux<SmartPlugDto> smartPlugEventStream = telemetryStream.getStream(SMART_PLUG, deviceId3);

        StepVerifier.create(doorSensorStream)
                .expectNext(mapDoorSensor(doorSensor))
                .thenCancel()
                .verify();

        StepVerifier.create(thermostatStream)
                .expectNext(mapThermostat(thermostat))
                .thenCancel()
                .verify();

        StepVerifier.create(smartPlugEventStream)
                .expectNext(mapSmartPlug(smartPlug))
                .thenCancel()
                .verify();

        sendMessage(kafkaProducer.sendMessage(doorSensor, deviceId1));
        sendMessage(kafkaProducer.sendMessage(thermostat, deviceId2));
        sendMessage(kafkaProducer.sendMessage(smartPlug, deviceId3));

        verify(telemetryStream, timeout(1000)).publish(doorSensor);
        verify(telemetryStream, timeout(25000).times(4)).publish(thermostat);
        verify(telemetryStream).publish(smartPlug);
    }

    @Test
    void skipNonRetriableErrors() {
        when(telemetryStream.publish(doorSensor)).thenCallRealMethod();
        doReturn(Mono.error(new IllegalArgumentException("Skip Error"))).when(telemetryStream).publish(thermostat);
        when(telemetryStream.publish(smartPlug)).thenCallRealMethod();

        sendMessage(kafkaProducer.sendMessage(doorSensor, deviceId1));
        sendMessage(kafkaProducer.sendMessage(thermostat, deviceId2));
        sendMessage(kafkaProducer.sendMessage(smartPlug, deviceId3));

        Flux<DoorSensorDto> doorSensorStream = telemetryStream.getStream(DOOR_SENSOR, deviceId1);
        Flux<ThermostatDto> thermostatEvent = telemetryStream.getStream(THERMOSTAT, deviceId2);
        Flux<SmartPlugDto> smartPlugEventStream = telemetryStream.getStream(SMART_PLUG, deviceId3);

        StepVerifier.create(doorSensorStream)
                .expectNext(mapDoorSensor(doorSensor))
                .thenCancel()
                .verify();

        StepVerifier.create(thermostatEvent)
                .expectNextCount(0)
                .expectComplete()
                .verify();

        StepVerifier.create(smartPlugEventStream)
                .expectNext(mapSmartPlug(smartPlug))
                .thenCancel()
                .verify();

        verify(telemetryStream, timeout(1000)).publish(doorSensor);
        verify(telemetryStream, timeout(1000)).publish(thermostat);
        verify(telemetryStream, timeout(1000)).publish(smartPlug);
    }

    private void sendMessage(Mono<SenderResult<Void>> sendMessage) {
        StepVerifier.create(sendMessage)
                .expectNextCount(1)
                .verifyComplete();
    }
}