package com.iot.devices.management.analytics_visualisation_service.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iot.devices.management.analytics_visualisation_service.analytics.*;
import com.iot.devices.management.analytics_visualisation_service.dto.EnergyMeterDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.EnergyMeterEvent;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services.TelemetryService;
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
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.controllers.GlobalErrorWebExceptionHandler.MESSAGE;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.ENERGY_METER;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@WebFluxTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TelemetriesRouterFunction.class,
        DevicesHandler.class,
        GlobalErrorWebExceptionHandler.class,
        AnalyticManager.class,
        DoorSensorAnalyticProvider.class,
        EnergyMeterAnalyticProvider.class,
        SmartLightAnalyticProvider.class,
        SmartPlugAnalyticProvider.class,
        SoilMoistureAnalyticProvider.class,
        TemperatureSensorAnalyticProvider.class,
        ThermostatAnalyticProvider.class,
        WebProperties.Resources.class
})
class DevicesHandlerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ApplicationContext context;
    @MockitoBean
    TelemetryStream telemetryStream;
    @MockitoBean
    TelemetryService telemetryService;


    WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(telemetryService);
    }

    @Test
    void badRequestHistory() {
        EnergyMeterDto energyMeterDto = new EnergyMeterDto(UUID.randomUUID(), 220f, 1.1f, 340f,
                3000f, DeviceStatus.ONLINE, "asd", now().truncatedTo(MILLIS));

        EnergyMeterEvent event = mapToEvent(energyMeterDto);

        when(telemetryService.findByDeviceIdAndLastUpdatedBetween(eq(energyMeterDto.deviceId()), any(Instant.class), any(Instant.class), eq(ENERGY_METER)))
                .thenReturn(Flux.fromIterable(List.of(event)));

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
                3000f, DeviceStatus.ONLINE, "asd", now().truncatedTo(MILLIS));

        EnergyMeterEvent event = mapToEvent(energyMeterDto);

        when(telemetryService.findByDeviceIdAndLastUpdatedBetween(eq(energyMeterDto.deviceId()), any(Instant.class), any(Instant.class), eq(ENERGY_METER)))
                .thenThrow(DataAccessResourceFailureException.class);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/history")
                        .queryParam("from", "2025-01-01T00:00:00Z")
                        .queryParam("to", "2025-08-31T23:59:59Z")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .build(energyMeterDto.deviceId()))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(telemetryService).findByDeviceIdAndLastUpdatedBetween(eq(energyMeterDto.deviceId()), any(Instant.class), any(Instant.class), eq(ENERGY_METER));
    }

    @Test
    void isOkHistory() {
        UUID deviceId = UUID.randomUUID();
        EnergyMeterDto energyMeterDto1 = new EnergyMeterDto(deviceId, 220f, 1.1f, 340f,
                3000f, DeviceStatus.ONLINE, "asd", now().truncatedTo(MILLIS));

        EnergyMeterDto energyMeterDto2 = new EnergyMeterDto(deviceId, 223f, 1.1f, 340f,
                3049f, DeviceStatus.ONLINE, "asd", now().plus(5, SECONDS).truncatedTo(MILLIS));

        EnergyMeterEvent event1 = mapToEvent(energyMeterDto1);
        EnergyMeterEvent event2 = mapToEvent(energyMeterDto2);

        when(telemetryService.findByDeviceIdAndLastUpdatedBetween(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER)))
                .thenReturn(Flux.fromIterable(List.of(event1, event2)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/history")
                        .queryParam("from", "2025-01-01T00:00:00Z")
                        .queryParam("to", "2025-08-31T23:59:59Z")
                        .queryParam("deviceType", ENERGY_METER.getId())
                        .build(deviceId))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    try {
                        List<EnergyMeterDto> list = objectMapper.readValue(response, new TypeReference<>() {});
                        assertThat(list.getFirst()).isEqualTo(energyMeterDto1);
                        assertThat(list.get(1)).isEqualTo(energyMeterDto2);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        verify(telemetryService).findByDeviceIdAndLastUpdatedBetween(eq(deviceId), any(Instant.class), any(Instant.class), eq(ENERGY_METER));
    }

    @Test
    void badRequestRealTime() {
        EnergyMeterDto energyMeterDto = new EnergyMeterDto(UUID.randomUUID(), 220f, 1.1f, 340f,
                3000f, DeviceStatus.ONLINE, "asd", now().truncatedTo(MILLIS));

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
                3000f, DeviceStatus.ONLINE, "asd", now().truncatedTo(MILLIS));

        when(telemetryStream.getStream(ENERGY_METER, energyMeterDto.deviceId()))
                .thenThrow(NullPointerException.class);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/realTime")
                        .queryParam("from", "2025-01-01T00:00:00Z")
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
                3000f, DeviceStatus.ONLINE, "asd", now().truncatedTo(MILLIS));

        EnergyMeterDto energyMeterDto2 = new EnergyMeterDto(deviceId, 223f, 1.1f, 340f,
                3049f, DeviceStatus.ONLINE, "asd", now().plus(5, SECONDS).truncatedTo(MILLIS));

        when(telemetryStream.getStream(ENERGY_METER, deviceId)).thenReturn(Flux.fromIterable(List.of(energyMeterDto1, energyMeterDto2)));

        Flux<EnergyMeterDto> flux = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/devices/{deviceId}/realTime")
                        .queryParam("from", "2025-01-01T00:00:00Z")
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

    private EnergyMeterEvent mapToEvent(EnergyMeterDto energyMeterDto) {
        return EnergyMeterEvent.builder()
                .deviceId(energyMeterDto.deviceId())
                .voltage(energyMeterDto.voltage())
                .current(energyMeterDto.current())
                .power(energyMeterDto.power())
                .energyConsumed(energyMeterDto.energyConsumed())
                .firmwareVersion(energyMeterDto.firmwareVersion())
                .status(DeviceStatus.ONLINE)
                .lastUpdated(energyMeterDto.lastUpdated())
                .build();
    }
}