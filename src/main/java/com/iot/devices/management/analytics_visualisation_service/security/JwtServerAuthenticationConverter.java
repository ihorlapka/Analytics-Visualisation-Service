package com.iot.devices.management.analytics_visualisation_service.security;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.User;
import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final String BEARER = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER))
                .map(header -> header.substring(BEARER.length()))
                .flatMap(token -> createUserDetails(token)
                        .map(user -> new JwtToken(token, user)));
    }

    private Mono<User> createUserDetails(String token) {
        final String username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username);
    }
}
