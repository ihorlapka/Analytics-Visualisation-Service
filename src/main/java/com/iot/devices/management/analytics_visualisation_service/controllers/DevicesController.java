package com.iot.devices.management.analytics_visualisation_service.controllers;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo.SmartPlugRepository;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class DevicesController {

    private final DeviceRepository deviceRepository;
    private final SmartPlugRepository smartPlugRepository;


    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello from Analytics Service!");
    }

    @GetMapping("/devices/{id}")
    public Flux<SmartPlugEvent> getDevices(@PathVariable UUID id) {
        return deviceRepository.findById(id)
                .flatMapMany(x -> smartPlugRepository.findAllById(List.of(id)))
                .takeLast(5);
    }
}
