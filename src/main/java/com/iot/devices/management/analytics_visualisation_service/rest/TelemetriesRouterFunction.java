package com.iot.devices.management.analytics_visualisation_service.rest;

import com.iot.devices.management.analytics_visualisation_service.openapi.RouterFunctionOpenApi;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TelemetriesRouterFunction {

    @Bean
    @RouterFunctionOpenApi
    public RouterFunction<ServerResponse> deviceRouterFunction(DevicesHandler deviceHandler) {
        return route()
                .nest(path("/api/v1/devices/{deviceId}"), builder ->
                        builder.GET("/history", deviceHandler::getTelemetryHistory)
                                .GET("/realTime", deviceHandler::getRealTimeTelemetry)
                                .GET("/historyWithRealTime", deviceHandler::getTelemetryHistoryWithRealTimeData)
                                .GET("/analytics", deviceHandler::getAnalytics)
                                .GET("/lastTelemetry", deviceHandler::getLastTelemetry)
                                .GET("/alertHistory", deviceHandler::getAlertsHistory)
                                .GET("/realTimeAlerts", deviceHandler::getRealTimeAlerts)
                                .GET("/historyWithRealTimeAlerts", deviceHandler::getHistoryWithRealTimeAlerts)
                                .GET("/lastAlert", deviceHandler::getLastAlert))
                .build()
                .and(route()
                        .nest(path("api/v1"), builder ->
                                builder.GET("/devicesPerManufacturer", deviceHandler::getAmountOfDevicesPerManufacturer)
                                        .GET("/statuses", deviceHandler::getAmountOfDevicesPerStatus))
                        .build())
                .and(route(GET("/.well-known/appspecific/com.chrome.devtools.json"), //just for skipping errors
                        request -> ServerResponse.notFound().build()));
    }

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }
}
