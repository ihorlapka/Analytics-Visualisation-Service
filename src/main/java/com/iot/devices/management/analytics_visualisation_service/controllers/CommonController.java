package com.iot.devices.management.analytics_visualisation_service.controllers;

import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.SmartPlugEvent;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.DeviceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
public class CommonController {

//    private final DeviceRepository deviceRepository
//
//    @GetMapping("/devices/{deviceId}")
//    public Flux<SmartPlugEvent> getDevice(@PathVariable UUID deviceId) {
//        return deviceRepository.findById(deviceId)
//                .flatMapMany(x -> smartPlugRepository.findAllById(List.of(deviceId)))
//                .takeLast(5);
//    }
}
