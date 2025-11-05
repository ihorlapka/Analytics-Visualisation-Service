package com.iot.devices.management.analytics_visualisation_service.dto;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.SeverityLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "Alert", description = "Alert")
public record AlertDto(
        @NonNull UUID alertId,
        @NonNull UUID deviceId,
        @NonNull UUID ruleId,
        @NonNull SeverityLevel severity,
        @NonNull Instant timestamp,
        @NonNull String message,
        @Nullable Float actualValue) {
}
