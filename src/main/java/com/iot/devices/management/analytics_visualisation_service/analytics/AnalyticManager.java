package com.iot.devices.management.analytics_visualisation_service.analytics;

import com.google.common.collect.ImmutableMap;
import com.iot.devices.management.analytics_visualisation_service.analytics.model.Analytic;
import com.iot.devices.management.analytics_visualisation_service.dto.TelemetryDto;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.*;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.SMART_PLUG;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.SOIL_MOISTURE_SENSOR;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.TEMPERATURE_SENSOR;
import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType.THERMOSTAT;

@Component
public class AnalyticManager implements AnalyticRegistry {

    private final Map<DeviceType, AnalyticProvider<? extends TelemetryDto, ? extends Analytic>> analyticByType;

    public AnalyticManager(DoorSensorAnalyticProvider doorSensorAnalytic,
                           EnergyMeterAnalyticProvider energyMeterAnalytic,
                           SmartLightAnalyticProvider smartLightAnalytic,
                           SmartPlugAnalyticProvider smartPlugAnalytic,
                           SoilMoistureAnalyticProvider soilMoistureAnalytic,
                           TemperatureSensorAnalyticProvider temperatureSensorAnalytic,
                           ThermostatAnalyticProvider thermostatAnalytic) {
        this.analyticByType = ImmutableMap.<DeviceType, AnalyticProvider<? extends TelemetryDto, ? extends Analytic>>builder()
                .put(DOOR_SENSOR, doorSensorAnalytic)
                .put(ENERGY_METER, energyMeterAnalytic)
                .put(SMART_LIGHT, smartLightAnalytic)
                .put(SMART_PLUG, smartPlugAnalytic)
                .put(SOIL_MOISTURE_SENSOR, soilMoistureAnalytic)
                .put(TEMPERATURE_SENSOR, temperatureSensorAnalytic)
                .put(THERMOSTAT, thermostatAnalytic)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TelemetryDto, U extends Analytic> AnalyticProvider<T, U> getProvider(DeviceType type) {
        final AnalyticProvider<?, ?> provider = analyticByType.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported device type: " + type);
        }
        return (AnalyticProvider<T, U>) provider;
    }
}
