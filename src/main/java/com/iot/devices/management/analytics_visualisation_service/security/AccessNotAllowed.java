package com.iot.devices.management.analytics_visualisation_service.security;

public class AccessNotAllowed extends RuntimeException {

    public AccessNotAllowed(String message) {
        super(message);
    }
}
