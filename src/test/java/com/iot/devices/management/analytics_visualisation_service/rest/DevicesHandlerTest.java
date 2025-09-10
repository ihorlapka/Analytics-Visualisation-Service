package com.iot.devices.management.analytics_visualisation_service.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iot.devices.management.analytics_visualisation_service.analytics.*;
import com.iot.devices.management.analytics_visualisation_service.analytics.model.EnergyMeterAnalytic;
import com.iot.devices.management.analytics_visualisation_service.dto.EnergyMeterDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.cache.TelemetryCachingRepository;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.Device;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.DeviceRepository;
import com.iot.devices.management.analytics_visualisation_service.stream.TelemetryStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.rest.GlobalErrorWebExceptionHandler.MESSAGE;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceManufacturer.*;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus.*;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.*;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static reactor.core.publisher.Flux.fromIterable;

@WebFluxTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TelemetriesRouterFunction.class,
        DevicesHandler.class,
        GlobalErrorWebExceptionHandler.class,
        ThermostatAnalyticProvider.class,
        WebProperties.Resources.class
})
class DevicesHandlerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ApplicationContext context;
    @MockitoBean
    AnalyticRegistry analyticRegistry;
    @MockitoBean
    TelemetryStream telemetryStream;
    @MockitoBean
    TelemetryCachingRepository telemetryCachingRepository;
    @MockitoBean
    DeviceRepository deviceRepository;

    WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(telemetryCachingRepository, telemetryStream, analyticRegistry, deviceRepository);
    }

    @Test
    void badRequestHistory() {
        EnergyMeterDto energyMeterDto = new EnergyMeterDto(UUID.randomUUID(), 220f, 1.1f, 340f,
                3000f, ONLINE, "asd", now().truncatedTo(MILLIS));

        doReturn(Mono.just(energyMeterDto)).when(telemetryCachingRepository).getFromCacheOrDb(eq(energyMeterDto.deviceId()), any(Instant.class), any(Instant.class), eq(ENERGY_METER));

        webTestClient.get()
                .uri("/api/v1/devices/" + energyMeterDto.getDeviceId() + "/history")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(response -> {
                    assertThat(response.get(MESSAGE)).isEqualTo("Some params in request are not set properly!");
                });
    }

    @Test
    void serverErrorHistory() {
        EnergyMeterDto energyMeterDto = new EnergyMeterDto(UUID.randomUUID(), 220f, 1.1f, 340f,
                3000f, ONLINE, "asd", now().truncatedTo(MILLIS));

        when(telemetryCachingRepository.getFromCacheOrDb(eq(energyMeterDto.deviceId()), any(Instant.class), any(Instant.class), eq(ENERGY_METER)))
                .thenThrow(DataAccessResourceFailureException.class);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/history")
                        .queryParam("from", "2025-01-01T00:00")
                        .queryParam("to", "2025-08-31T23:59:59")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .build(energyMeterDto.deviceId()))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(telemetryCachingRepository).getFromCacheOrDb(eq(energyMeterDto.deviceId()), any(Instant.class), any(Instant.class), eq(ENERGY_METER));
    }

    @Test
    void isOkHistory() {
        UUID deviceId = UUID.randomUUID();
        EnergyMeterDto energyMeterDto1 = new EnergyMeterDto(deviceId, 220f, 1.1f, 340f,
                3000f, ONLINE, "asd", now().truncatedTo(MILLIS));

        EnergyMeterDto energyMeterDto2 = new EnergyMeterDto(deviceId, 223f, 1.1f, 340f,
                3049f, ONLINE, "asd", now().plus(5, SECONDS).truncatedTo(MILLIS));

        doReturn(Mono.just(List.of(energyMeterDto1, energyMeterDto2))).when(telemetryCachingRepository).getFromCacheOrDb(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/history")
                        .queryParam("from", "2025-01-01T00:00")
                        .queryParam("to", "2025-08-31T23:59")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .build(deviceId))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    try {
                        List<EnergyMeterDto> list = objectMapper.readValue(response, new TypeReference<>() {
                        });
                        assertThat(list.getFirst()).isEqualTo(energyMeterDto1);
                        assertThat(list.get(1)).isEqualTo(energyMeterDto2);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        verify(telemetryCachingRepository).getFromCacheOrDb(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER));
    }

    @Test
    void badRequestRealTime() {
        EnergyMeterDto energyMeterDto = new EnergyMeterDto(UUID.randomUUID(), 220f, 1.1f, 340f,
                3000f, ONLINE, "asd", now().truncatedTo(MILLIS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/realTime")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .build(energyMeterDto.deviceId()))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(response -> {
                    assertThat(response.get(MESSAGE)).isEqualTo("Some params in request are not set properly!");
                });
    }

    @Test
    void serverErrorRealTime() {
        EnergyMeterDto energyMeterDto = new EnergyMeterDto(UUID.randomUUID(), 220f, 1.1f, 340f,
                3000f, ONLINE, "asd", now().truncatedTo(MILLIS));

        when(telemetryStream.getStream(ENERGY_METER, energyMeterDto.deviceId()))
                .thenThrow(NullPointerException.class);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/realTime")
                        .queryParam("from", "2025-01-01T00:00:00")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .queryParam("rate", 10)
                        .build(energyMeterDto.deviceId()))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(telemetryStream).getStream(ENERGY_METER, energyMeterDto.deviceId());
    }

    @Test
    void isOkRealTime() {
        UUID deviceId = UUID.randomUUID();
        EnergyMeterDto energyMeterDto1 = new EnergyMeterDto(deviceId, 220f, 1.1f, 340f,
                3000f, ONLINE, "asd", now().truncatedTo(MILLIS));

        EnergyMeterDto energyMeterDto2 = new EnergyMeterDto(deviceId, 223f, 1.1f, 340f,
                3049f, ONLINE, "asd", now().plus(5, SECONDS).truncatedTo(MILLIS));

        when(telemetryStream.getStream(ENERGY_METER, deviceId)).thenReturn(fromIterable(List.of(energyMeterDto1, energyMeterDto2)));

        Flux<EnergyMeterDto> flux = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/realTime")
                        .queryParam("from", "2025-01-01T00:00:00")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .queryParam("rate", 10)
                        .build(deviceId))
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .returnResult(EnergyMeterDto.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNextMatches(dto -> dto.getDeviceId().equals(deviceId))
                .expectNextCount(1)
                .thenCancel()
                .verify();

        verify(telemetryStream).getStream(ENERGY_METER, deviceId);
    }

    @Test
    void isOkHistoryWithRealTime() {
        UUID deviceId = UUID.randomUUID();
        EnergyMeterDto energyMeterDto1 = new EnergyMeterDto(deviceId, 220f, 1.1f, 340f,
                3000f, ONLINE, "asd", now().minus(10, SECONDS).truncatedTo(MILLIS));

        EnergyMeterDto energyMeterDto2 = new EnergyMeterDto(deviceId, 223f, 1.1f, 340f,
                3049f, ONLINE, "asd", now().minus(5, SECONDS).truncatedTo(MILLIS));

        EnergyMeterDto energyMeterDto3 = new EnergyMeterDto(deviceId, null, null, 340f,
                null, ONLINE, "asd", now().truncatedTo(MILLIS));

        doReturn(Mono.just(List.of(energyMeterDto1, energyMeterDto2))).when(telemetryCachingRepository).getFromCacheOrDb(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER));

        when(telemetryStream.getStream(ENERGY_METER, deviceId))
                .thenReturn(fromIterable(List.of(energyMeterDto3, energyMeterDto2)));

        Flux<EnergyMeterDto> flux = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/historyWithRealTime")
                        .queryParam("from", "2025-01-01T00:00:00")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .build(deviceId))
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .returnResult(EnergyMeterDto.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNextMatches(dto -> dto.getDeviceId().equals(deviceId))
                .expectNextCount(2)
                .thenCancel()
                .verify();

        verify(telemetryStream).getStream(ENERGY_METER, deviceId);
        verify(telemetryCachingRepository).getFromCacheOrDb(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER));
    }

    @Test
    void isOkAnalytic() {
        UUID deviceId = UUID.randomUUID();
        EnergyMeterDto energyMeterDto1 = new EnergyMeterDto(deviceId, 220f, 0.8f, 340f,
                3000f, ONLINE, "asd", now().truncatedTo(MILLIS));

        EnergyMeterDto energyMeterDto2 = new EnergyMeterDto(deviceId, 224f, 1.2f, 360f,
                4000f, ONLINE, "asd", now().plus(5, SECONDS).truncatedTo(MILLIS));

        doReturn(Mono.just(List.of(energyMeterDto1, energyMeterDto2))).when(telemetryCachingRepository).getFromCacheOrDb(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER));

        AnalyticProvider<EnergyMeterDto, EnergyMeterAnalytic> energyMeterAnalyticProvider = EnergyMeterAnalyticProvider.of();
        doReturn(energyMeterAnalyticProvider).when(analyticRegistry).getProvider(ENERGY_METER);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/analytics")
                        .queryParam("from", "2025-01-01T00:00:00")
                        .queryParam("to", "2025-08-31T23:59:59")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .build(deviceId))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    try {
                        EnergyMeterAnalytic analytic = objectMapper.readValue(response, EnergyMeterAnalytic.class);
                        EnergyMeterAnalytic expected = EnergyMeterAnalytic.of(
                                222f, 1f, 350f, 3500f,
                                224f, 1.2f, 360f, 4000f,
                                220f, 0.8f, 340f, 3000f
                        );
                        assertThat(analytic).isEqualTo(expected);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        verify(telemetryCachingRepository).getFromCacheOrDb(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER));
        verify(analyticRegistry).getProvider(ENERGY_METER);
    }

    @Test
    void isOkDevicesPerManufacturer() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z");

        Device device1 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", HONEYWELL, "Model-45",
                ENERGY_METER, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), ONLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device2 = new Device(UUID.randomUUID(), "EnergyMeter 904", "SN-23251d7f569986ad70862ff218002c65", SAMSUNG, "Model-44",
                ENERGY_METER, "Installed in LivingRoom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), ONLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device3 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", SIEMENS, "Model-45",
                DOOR_SENSOR, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), OFFLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device4 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", SIEMENS, "Model-45",
                ENERGY_METER, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), ONLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device5 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", HONEYWELL, "Model-45",
                ENERGY_METER, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), MAINTENANCE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        when(deviceRepository.findAll()).thenReturn(Flux.just(device1, device2, device3, device4, device5));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devicesPerManufacturer")
                        .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    assertThat(response.get(HONEYWELL.name())).isEqualTo(2);
                    assertThat(response.get(SIEMENS.name())).isEqualTo(2);
                    assertThat(response.get(SAMSUNG.name())).isEqualTo(1);
                });

        verify(deviceRepository).findAll();
    }

    @Test
    void isOkStatuses() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z");

        Device device1 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", HONEYWELL, "Model-45",
                ENERGY_METER, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), ONLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device2 = new Device(UUID.randomUUID(), "EnergyMeter 904", "SN-23251d7f569986ad70862ff218002c65", SAMSUNG, "Model-44",
                ENERGY_METER, "Installed in LivingRoom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), ONLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device3 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", SIEMENS, "Model-45",
                DOOR_SENSOR, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), OFFLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device4 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", SIEMENS, "Model-45",
                SMART_PLUG, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), ONLINE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        Device device5 = new Device(UUID.randomUUID(), "EnergyMeter 903", "SN-23251d7f569986ad70862ff218002c64", HONEYWELL, "Model-45",
                SOIL_MOISTURE_SENSOR, "Installed in Bathroom", BigDecimal.valueOf(48.995857), BigDecimal.valueOf(24.797740), UUID.randomUUID(), MAINTENANCE,
                OffsetDateTime.parse("2025-07-12 08:56:50.278 +0300", formatter), "2.1", OffsetDateTime.parse("2025-08-09 17:50:13.896 +0300", formatter),
                OffsetDateTime.parse("2025-08-19 15:49:23.676 +0300", formatter),
                "{\"power\": 384.3927, \"current\": 1.7420197, \"voltage\": 220.65923, \"energyConsumed\": 76074210}");

        when(deviceRepository.findAll()).thenReturn(Flux.just(device1, device2, device3, device4, device5));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/statuses")
                        .queryParam("status", ONLINE)
                        .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    assertThat(response.get(HONEYWELL.name())).isEqualTo(1);
                    assertThat(response.get(SIEMENS.name())).isEqualTo(1);
                    assertThat(response.get(SAMSUNG.name())).isEqualTo(1);
                });

        verify(deviceRepository).findAll();
    }
}