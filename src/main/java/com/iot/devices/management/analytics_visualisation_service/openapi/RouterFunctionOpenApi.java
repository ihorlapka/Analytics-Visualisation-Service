package com.iot.devices.management.analytics_visualisation_service.openapi;

import com.iot.devices.management.analytics_visualisation_service.analytics.model.*;
import com.iot.devices.management.analytics_visualisation_service.rest.DevicesHandler;
import com.iot.devices.management.analytics_visualisation_service.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RouterOperations({
        @RouterOperation(path = "/api/v1/devices/{deviceId}/history",
                beanClass = DevicesHandler.class,
                beanMethod = "getHistory",
                operation = @Operation(operationId = "getHistory",
                        summary = "Get historical data for a device",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device", example = "5d27d69f-3254-43d7-8e5d-a68a39a45d92"),
                                @Parameter(name = "from", required = true, description = "Start timestamp (ISO-8601)", example = "2025-08-17T13:19:00.000Z"),
                                @Parameter(name = "to", required = true, description = "End timestamp (ISO-8601)", example = "2025-08-17T13:19:10.000Z"),
                                @Parameter(
                                        name = "deviceType",
                                        required = true,
                                        description = "The Type of the device",
                                        schema = @Schema(
                                                implementation = String.class,
                                                allowableValues = {
                                                        RouterFunctionOpenApi.DOOR_SENSOR,
                                                        RouterFunctionOpenApi.ENERGY_METER,
                                                        RouterFunctionOpenApi.SMART_PLUG,
                                                        RouterFunctionOpenApi.SMART_LIGHT,
                                                        RouterFunctionOpenApi.TEMPERATURE_SENSOR,
                                                        RouterFunctionOpenApi.THERMOSTAT,
                                                        RouterFunctionOpenApi.SOIL_MOISTURE_SENSOR
                                                }
                                        )
                                )
                        },
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "Telemetries history",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                array = @ArraySchema(
                                                        schema = @Schema(
                                                                oneOf = {
                                                                        DoorSensorDto.class,
                                                                        EnergyMeterDto.class,
                                                                        SmartLightDto.class,
                                                                        SmartPlugDto.class,
                                                                        SoilMoistureSensorDto.class,
                                                                        TemperatureSensorDto.class,
                                                                        ThermostatDto.class
                                                                }
                                                        )
                                                ),
                                                examples = @ExampleObject(
                                                        name = "EnergyMeter",
                                                        summary = "Energy Meter telemetries history",
                                                        value = """
                                                                [ {
                                                                  "deviceId" : "5d27d69f-3254-43d7-8e5d-a68a39a45d92",
                                                                  "voltage" : 220.0,
                                                                  "current" : 1.1,
                                                                  "power" : 340.0,
                                                                  "energyConsumed" : 3000.0,
                                                                  "status" : "ONLINE",
                                                                  "firmwareVersion" : "asd",
                                                                  "lastUpdated" : "2025-09-04T14:24:33.052Z"
                                                                }, {
                                                                  "deviceId" : "5d27d69f-3254-43d7-8e5d-a68a39a45d92",
                                                                  "voltage" : 223.0,
                                                                  "current" : 1.1,
                                                                  "power" : 340.0,
                                                                  "energyConsumed" : 3049.0,
                                                                  "status" : "ONLINE",
                                                                  "firmwareVersion" : "asd",
                                                                  "lastUpdated" : "2025-09-04T14:24:38.052Z"
                                                                } ]
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Request is invalid",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Bad request",
                                                        summary = "Request is invalid",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:22:11.864+00:00",
                                                                "path":"/api/v1/devices/0752023b-57a3-4665-a842-b4da71aefcdc/history",
                                                                "status":400,
                                                                "error":"Client Error",
                                                                "requestId":"585c9d89",
                                                                "message":"Some params in request are not set properly!"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "500",
                                        description = "Internal Server Error",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Server error",
                                                        summary = "Server is down",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:19:57.120+00:00",
                                                                "path":"/api/v1/devices/3dd253d3-901d-4f80-a240-2c8b983f0ee4/history",
                                                                "status":500,
                                                                "error":"Internal Server Error",
                                                                "requestId":"5f60ed6"
                                                                }
                                                                """
                                                )
                                        )
                                )
                        })
        ),
        @RouterOperation(path = "/api/v1/devices/{deviceId}/realTime",
                beanClass = DevicesHandler.class,
                beanMethod = "getRealTimeTelemetry",
                operation = @Operation(operationId = "getRealTimeTelemetry",
                        summary = "Stream real-time data for a device",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device", example = "5d27d69f-3254-43d7-8e5d-a68a39a45d92"),
                                @Parameter(name = "rate", required = true, description = "Rate limit for the stream", example = "100"),
                                @Parameter(
                                        name = "deviceType",
                                        required = true,
                                        description = "The Type of the device",
                                        schema = @Schema(
                                                implementation = String.class,
                                                allowableValues = {
                                                        RouterFunctionOpenApi.DOOR_SENSOR,
                                                        RouterFunctionOpenApi.ENERGY_METER,
                                                        RouterFunctionOpenApi.SMART_PLUG,
                                                        RouterFunctionOpenApi.SMART_LIGHT,
                                                        RouterFunctionOpenApi.TEMPERATURE_SENSOR,
                                                        RouterFunctionOpenApi.THERMOSTAT,
                                                        RouterFunctionOpenApi.SOIL_MOISTURE_SENSOR
                                                }
                                        )
                                )
                        },
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "Telemetries stream",
                                        content = @Content(
                                                mediaType = TEXT_EVENT_STREAM_VALUE,
                                                array = @ArraySchema(
                                                        schema = @Schema(
                                                                oneOf = {
                                                                        DoorSensorDto.class,
                                                                        EnergyMeterDto.class,
                                                                        SmartLightDto.class,
                                                                        SmartPlugDto.class,
                                                                        SoilMoistureSensorDto.class,
                                                                        TemperatureSensorDto.class,
                                                                        ThermostatDto.class
                                                                }
                                                        )
                                                ),
                                                examples = @ExampleObject(
                                                        name = "EnergyMeter",
                                                        summary = "Energy Meter telemetries stream",
                                                        value = """
                                                                {
                                                                    "deviceId": "cd6e4534-7e64-4fc0-8321-8db428de9743",
                                                                    "voltage": 230.0,
                                                                    "current": 4.0,
                                                                    "power": 800.0,
                                                                    "energyConsumed": 0.0,
                                                                    "status": "ONLINE",
                                                                    "firmwareVersion": "2.1",
                                                                    "lastUpdated": "2025-09-04T15:55:45.197Z"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Request is invalid",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Bad request",
                                                        summary = "Request is invalid",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:22:11.864+00:00",
                                                                "path":"/api/v1/devices/0752023b-57a3-4665-a842-b4da71aefcdc/realTime",
                                                                "status":400,
                                                                "error":"Client Error",
                                                                "requestId":"585c9d89",
                                                                "message":"Some params in request are not set properly!"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "500",
                                        description = "Internal Server Error",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Server error",
                                                        summary = "Server is down",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:19:57.120+00:00",
                                                                "path":"/api/v1/devices/3dd253d3-901d-4f80-a240-2c8b983f0ee4/realTime",
                                                                "status":500,
                                                                "error":"Internal Server Error",
                                                                "requestId":"5f60ed6"
                                                                }
                                                                """
                                                )
                                        )
                                )
                        }
                )),
        @RouterOperation(path = "/api/v1/devices/{deviceId}/historyWithRealTime",
                beanClass = DevicesHandler.class,
                beanMethod = "getHistoryWithRealTimeData",
                operation = @Operation(operationId = "getHistoryWithRealTimeData",
                        summary = "Get historical data and then stream real-time data",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device", example = "5d27d69f-3254-43d7-8e5d-a68a39a45d92"),
                                @Parameter(name = "from", required = true, description = "Start timestamp (ISO-8601)", example = "2025-08-17T13:19:00.000Z"),
                                @Parameter(
                                        name = "deviceType",
                                        required = true,
                                        description = "The Type of the device",
                                        schema = @Schema(
                                                implementation = String.class,
                                                allowableValues = {
                                                        RouterFunctionOpenApi.DOOR_SENSOR,
                                                        RouterFunctionOpenApi.ENERGY_METER,
                                                        RouterFunctionOpenApi.SMART_PLUG,
                                                        RouterFunctionOpenApi.SMART_LIGHT,
                                                        RouterFunctionOpenApi.TEMPERATURE_SENSOR,
                                                        RouterFunctionOpenApi.THERMOSTAT,
                                                        RouterFunctionOpenApi.SOIL_MOISTURE_SENSOR
                                                }
                                        )
                                )
                        },
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "Telemetries history and real time stream",
                                        content = @Content(
                                                mediaType = TEXT_EVENT_STREAM_VALUE,
                                                array = @ArraySchema(
                                                        schema = @Schema(
                                                                oneOf = {
                                                                        DoorSensorDto.class,
                                                                        EnergyMeterDto.class,
                                                                        SmartLightDto.class,
                                                                        SmartPlugDto.class,
                                                                        SoilMoistureSensorDto.class,
                                                                        TemperatureSensorDto.class,
                                                                        ThermostatDto.class
                                                                }
                                                        )
                                                ),
                                                examples = @ExampleObject(
                                                        name = "EnergyMeter",
                                                        summary = "Energy Meter telemetries stream",
                                                        value = """
                                                                {
                                                                    "deviceId": "cd6e4534-7e64-4fc0-8321-8db428de9743",
                                                                    "voltage": 230.0,
                                                                    "current": 4.0,
                                                                    "power": 800.0,
                                                                    "energyConsumed": 0.0,
                                                                    "status": "ONLINE",
                                                                    "firmwareVersion": "2.1",
                                                                    "lastUpdated": "2025-09-04T15:55:45.197Z"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Request is invalid",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Bad request",
                                                        summary = "Request is invalid",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:22:11.864+00:00",
                                                                "path":"/api/v1/devices/0752023b-57a3-4665-a842-b4da71aefcdc/historyWithRealTime",
                                                                "status":400,
                                                                "error":"Client Error",
                                                                "requestId":"585c9d89",
                                                                "message":"Some params in request are not set properly!"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "500",
                                        description = "Internal Server Error",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Server error",
                                                        summary = "Server is down",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:19:57.120+00:00",
                                                                "path":"/api/v1/devices/3dd253d3-901d-4f80-a240-2c8b983f0ee4/historyWithRealTime",
                                                                "status":500,
                                                                "error":"Internal Server Error",
                                                                "requestId":"5f60ed6"
                                                                }
                                                                """
                                                )
                                        )
                                )
                        })
        ),
        @RouterOperation(path = "/api/v1/devices/{deviceId}/analytics",
                beanClass = DevicesHandler.class,
                beanMethod = "getAnalytics",
                operation = @Operation(operationId = "getAnalytics",
                        summary = "Get analytics for a specific device",
                        parameters = {
                                @Parameter(name = "deviceId", required = true, description = "The ID of the device", example = "5d27d69f-3254-43d7-8e5d-a68a39a45d92"),
                                @Parameter(name = "from", required = true, description = "Start timestamp (ISO-8601)", example = "2025-08-17T13:19:00.000Z"),
                                @Parameter(name = "to", required = true, description = "End timestamp (ISO-8601)", example = "2025-08-17T13:19:10.000Z"),
                                @Parameter(
                                        name = "deviceType",
                                        required = true,
                                        description = "The Type of the device",
                                        schema = @Schema(
                                                implementation = String.class,
                                                allowableValues = {
                                                        RouterFunctionOpenApi.DOOR_SENSOR,
                                                        RouterFunctionOpenApi.ENERGY_METER,
                                                        RouterFunctionOpenApi.SMART_PLUG,
                                                        RouterFunctionOpenApi.SMART_LIGHT,
                                                        RouterFunctionOpenApi.TEMPERATURE_SENSOR,
                                                        RouterFunctionOpenApi.THERMOSTAT,
                                                        RouterFunctionOpenApi.SOIL_MOISTURE_SENSOR
                                                }
                                        )
                                )
                        },
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "Telemetries analytics",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                array = @ArraySchema(
                                                        schema = @Schema(
                                                                oneOf = {
                                                                        DoorSensorAnalytic.class,
                                                                        EnergyMeterAnalytic.class,
                                                                        SmartLightAnalytic.class,
                                                                        SmartPlugAnalytic.class,
                                                                        SoilMoistureAnalytic.class,
                                                                        TemperatureSensorAnalytic.class,
                                                                        ThermostatAnalytic.class
                                                                }
                                                        )
                                                ),
                                                examples = @ExampleObject(
                                                        name = "EnergyMeterAnalytic",
                                                        summary = "Energy Meter telemetries analytic",
                                                        value = """
                                                                {
                                                                  "avgVoltage" : 222.0,
                                                                  "avgCurrent" : 1.0,
                                                                  "avgPower" : 350.0,
                                                                  "avgEnergyConsumed" : 3500.0,
                                                                  "maxVoltage" : 224.0,
                                                                  "maxCurrent" : 1.2,
                                                                  "maxPower" : 360.0,
                                                                  "maxEnergyConsumed" : 4000.0,
                                                                  "minVoltage" : 220.0,
                                                                  "minCurrent" : 0.8,
                                                                  "minPower" : 340.0,
                                                                  "minEnergyConsumed" : 3000.0
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Request is invalid",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Bad request",
                                                        summary = "Request is invalid",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:22:11.864+00:00",
                                                                "path":"/api/v1/devices/0752023b-57a3-4665-a842-b4da71aefcdc/analytics",
                                                                "status":400,
                                                                "error":"Client Error",
                                                                "requestId":"585c9d89",
                                                                "message":"Some params in request are not set properly!"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "500",
                                        description = "Internal Server Error",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Server error",
                                                        summary = "Server is down",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:19:57.120+00:00",
                                                                "path":"/api/v1/devices/3dd253d3-901d-4f80-a240-2c8b983f0ee4/analytics",
                                                                "status":500,
                                                                "error":"Internal Server Error",
                                                                "requestId":"5f60ed6"
                                                                }
                                                                """
                                                )
                                        )
                                )
                        }
                )),
        @RouterOperation(path = "/api/v1/devicesPerManufacturer",
                beanClass = DevicesHandler.class,
                beanMethod = "getAmountOfDevicesPerManufacturer",
                operation = @Operation(operationId = "getAmountOfDevicesPerManufacturer",
                        summary = "Get the amount of devices per manufacturer",
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "Returns amount of devices per manufacturer",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                examples = @ExampleObject(
                                                        name = "AmountOfDevicesPerManufacturer",
                                                        summary = "The amount of devices per manufacturer",
                                                        value = """
                                                                {
                                                                    "BOSCH": 8,
                                                                    "SIEMENS": 8,
                                                                    "CISCO_SYSTEMS": 12,
                                                                    "SAMSUNG": 10,
                                                                    "FITBIT": 8,
                                                                    "AMAZON": 13,
                                                                    "PHILIPS_HUE": 9,
                                                                    "HONEYWELL": 8,
                                                                    "SCHNEIDER_ELECTRIC": 8,
                                                                    "TEXAS_INSTRUMENTS": 6
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Request is invalid",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Bad request",
                                                        summary = "Request is invalid",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:22:11.864+00:00",
                                                                "path":"/api/v1/devicesPerManufacturer",
                                                                "status":400,
                                                                "error":"Client Error",
                                                                "requestId":"585c9d89",
                                                                "message":"Some params in request are not set properly!"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "500",
                                        description = "Internal Server Error",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Server error",
                                                        summary = "Server is down",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:19:57.120+00:00",
                                                                "path":"/api/v1/devicesPerManufacturer",
                                                                "status":500,
                                                                "error":"Internal Server Error",
                                                                "requestId":"5f60ed6"
                                                                }
                                                                """
                                                )
                                        )
                                )
                        }
                )
        ),
        @RouterOperation(path = "/api/v1/statuses",
                beanClass = DevicesHandler.class,
                beanMethod = "getAmountOfDevicesWithStatus",
                operation = @Operation(operationId = "getAmountOfDevicesWithStatus",
                        summary = "Get the number of devices with desired status",
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "Returns amount of devices per manufacturer with desired status",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                examples = @ExampleObject(
                                                        name = "AmountOfDevicesWithStatus",
                                                        summary = "The number of devices with desired status",
                                                        value = """
                                                                {
                                                                    "CISCO_SYSTEMS": 1,
                                                                    "PHILIPS_HUE": 1
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Request is invalid",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Bad request",
                                                        summary = "Request is invalid",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:22:11.864+00:00",
                                                                "path":"/api/v1/statuses",
                                                                "status":400,
                                                                "error":"Client Error",
                                                                "requestId":"585c9d89",
                                                                "message":"Some params in request are not set properly!"
                                                                }
                                                                """
                                                )
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "500",
                                        description = "Internal Server Error",
                                        content = @Content(
                                                mediaType = APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = String.class),
                                                examples = @ExampleObject(
                                                        name = "Server error",
                                                        summary = "Server is down",
                                                        value = """
                                                                {
                                                                "timestamp":"2025-09-04T13:19:57.120+00:00",
                                                                "path":"/api/v1/statuses",
                                                                "status":500,
                                                                "error":"Internal Server Error",
                                                                "requestId":"5f60ed6"
                                                                }
                                                                """
                                                )
                                        )
                                )
                        }
                )
        )
})
public @interface RouterFunctionOpenApi {

    String DOOR_SENSOR = "DoorSensor";
    String ENERGY_METER = "EnergyMeter";
    String SMART_PLUG = "SmartPlug";
    String SMART_LIGHT = "SmartLight";
    String TEMPERATURE_SENSOR = "TemperatureSensor";
    String THERMOSTAT = "Thermostat";
    String SOIL_MOISTURE_SENSOR = "SoilMoistureSensor";
}
