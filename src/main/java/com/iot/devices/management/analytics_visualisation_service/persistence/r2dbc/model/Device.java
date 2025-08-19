package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceManufacturer;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Table(name = "devices")
@ToString
@AllArgsConstructor
public class Device {

    @Id
    private UUID id;
    private String name;
    private String serialNumber;
    private DeviceManufacturer manufacturer;
    private String model;
    private DeviceType deviceType;
    private String locationDescription;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private UUID ownerUserId;
    private DeviceStatus status;
    private OffsetDateTime lastActiveAt;
    private String firmwareVersion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String telemetry;
}
