package com.iot.devices.management.analytics_visualisation_service.openapi;

import com.iot.devices.management.analytics_visualisation_service.controllers.DevicesHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RouterOperations({
        @RouterOperation(path = "/api/v1/devices/{deviceId}/history",
                beanClass = DevicesHandler.class,
                beanMethod = "getHistory",
                operation = @Operation(operationId = "getHistory",
                        summary = "Get historical data for a device",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device"),
                                @Parameter(name = "from", required = true, description = "Start timestamp (ISO-8601)"),
                                @Parameter(name = "to", required = true, description = "End timestamp (ISO-8601)")
                        }
                )),
        @RouterOperation(path = "/api/v1/devices/{deviceId}/realTime",
                beanClass = DevicesHandler.class,
                beanMethod = "getRealTimeTelemetry",
                operation = @Operation(operationId = "getRealTimeTelemetry",
                        summary = "Stream real-time data for a device",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device"),
                                @Parameter(name = "rate", description = "Rate limit for the stream")
                        })),
        @RouterOperation(path = "/api/v1/devices/{deviceId}/historyWithRealTime",
                beanClass = DevicesHandler.class,
                beanMethod = "getHistoryWithRealTimeData",
                operation = @Operation(operationId = "getHistoryWithRealTimeData",
                        summary = "Get historical data and then stream real-time data",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device"),
                                @Parameter(name = "from", required = true, description = "Start timestamp (ISO-8601)")
                        })),
        @RouterOperation(path = "/api/v1/devices/{deviceId}/analytics",
                beanClass = DevicesHandler.class,
                beanMethod = "getAnalytics",
                operation = @Operation(operationId = "getAnalytics",
                        summary = "Get analytics for a specific device",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device")
                        })),
        @RouterOperation(path = "/api/v1/devicesPerManufacturer",
                beanClass = DevicesHandler.class,
                beanMethod = "getAmountOfDevicesPerManufacturer",
                operation = @Operation(operationId = "getAmountOfDevicesPerManufacturer",
                        summary = "Get the amount of devices per manufacturer")),
        @RouterOperation(path = "/api/v1/statuses",
                beanClass = DevicesHandler.class,
                beanMethod = "getAmountOfDevicesWithStatus",
                operation = @Operation(operationId = "getAmountOfDevicesWithStatus",
                        summary = "Get the number of devices with each status"))
})
public @interface RouterFunctionOpenApi {
    //TODO: refactor!
}
