package com.iot.devices.management.analytics_visualisation_service.security;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {

    JwtAuthenticationException(String msg) {
        super(msg);
    }
}
