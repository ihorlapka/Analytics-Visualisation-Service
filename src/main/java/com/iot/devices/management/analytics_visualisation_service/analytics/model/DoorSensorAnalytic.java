package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.*;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public final class DoorSensorAnalytic implements Analytic {

    Integer openCount;
    Boolean temperAlert;
}
