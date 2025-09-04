package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
@Schema(name = "DoorSensorAnalytic", description = "Door Sensor Analytic")
public final class DoorSensorAnalytic implements Analytic {

    Integer openCount;
    Boolean temperAlert;
}
