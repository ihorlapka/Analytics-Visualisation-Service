package com.iot.devices.management.analytics_visualisation_service.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TelemetriesRouterFunction {

    //TODO: add alerts endpoints!
    //TODO: add Swagger

    @Bean
    public RouterFunction<ServerResponse> deviceRouterFunction(DevicesHandler deviceHandler) {
        return route()
                .nest(path("/api/v1/devices/{deviceId}"), builder ->
                        builder.GET("/history", deviceHandler::getHistory)
                                .GET("/realTime", deviceHandler::getRealTimeTelemetry)
                                .GET("/historyWithRealTime", deviceHandler::getHistoryWithRealTimeData)
                                .GET("/analytics", deviceHandler::getAnalytics))
                .build()
                .and(route()
                        .nest(path("api/v1"), builder ->
                                builder.GET("/devicesPerManufacturer", deviceHandler::getAmountOfDevicesPerManufacturer)
                                        .GET("/statuses", deviceHandler::getAmountOfDevicesWithStatus))
                        .build());
    }
}
