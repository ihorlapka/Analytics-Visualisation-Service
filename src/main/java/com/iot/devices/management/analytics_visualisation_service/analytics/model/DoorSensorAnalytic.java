package com.iot.devices.management.analytics_visualisation_service.analytics.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class DoorSensorAnalytic implements Analytic {

    Integer openCount;
    Boolean temperAlert;
}
