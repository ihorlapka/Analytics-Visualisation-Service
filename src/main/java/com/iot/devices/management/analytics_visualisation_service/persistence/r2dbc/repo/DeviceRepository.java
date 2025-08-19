package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.Device;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface DeviceRepository extends R2dbcRepository<Device, UUID> {
}
