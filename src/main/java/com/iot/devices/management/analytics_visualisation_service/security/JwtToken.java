package com.iot.devices.management.analytics_visualisation_service.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class JwtToken extends AbstractAuthenticationToken {
    private final String token;
    private final UserDetails principal;

    JwtToken(String token, UserDetails principal) {
        super(principal.getAuthorities());
        this.token = token;
        this.principal = principal;
    }

    Authentication withAuthenticated(boolean isAuthenticated) {
        JwtToken cloned = new JwtToken(token, principal);
        cloned.setAuthenticated(isAuthenticated);
        return cloned;
    }

    @Override
    public Object getCredentials() {
        return principal.getPassword();
    }
}
