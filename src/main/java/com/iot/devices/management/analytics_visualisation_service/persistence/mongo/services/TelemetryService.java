package com.iot.devices.management.analytics_visualisation_service.persistence.mongo.services;

import com.google.common.collect.ImmutableMap;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.model.*;
import com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.*;

@Service
public class TelemetryService {

    public TelemetryService(DoorSensorRepository doorSensorRepository,
                            EnergyMeterRepository energyMeterRepository,
                            SmartLightRepository smartLightRepository,
                            SmartPlugRepository smartPlugRepository,
                            SoilMoistureSensorRepository soilMoistureSensorRepository,
                            TemperatureSensorRepository temperatureSensorRepository,
                            ThermostatRepository thermostatRepository) {
        this.repositoryByType = ImmutableMap.<DeviceType, TelemetryRepository>builder()
                .put(DOOR_SENSOR, doorSensorRepository)
                .put(ENERGY_METER, energyMeterRepository)
                .put(SMART_LIGHT, smartLightRepository)
                .put(SMART_PLUG, smartPlugRepository)
                .put(SOIL_MOISTURE_SENSOR, soilMoistureSensorRepository)
                .put(TEMPERATURE_SENSOR, temperatureSensorRepository)
                .put(THERMOSTAT, thermostatRepository)
                .build();
    }

    private final Map<DeviceType, TelemetryRepository> repositoryByType;


    public <T extends TelemetryEvent> Flux<T> findByDeviceIdAndLastUpdatedBetween(UUID deviceId, Instant from, Instant to, DeviceType deviceType) {
        return repositoryByType.get(deviceType).findByDeviceIdAndLastUpdatedBetween(deviceId, from, to);
    }
}
