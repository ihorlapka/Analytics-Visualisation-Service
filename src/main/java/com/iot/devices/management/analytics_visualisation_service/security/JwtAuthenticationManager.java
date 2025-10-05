package com.iot.devices.management.analytics_visualisation_service.security;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .cast(JwtToken.class)
                .filter(jwtToken -> jwtService.isTokenValid(jwtToken.getToken(), jwtToken.getPrincipal()))
                .filterWhen(jwtToken -> tokenRepository.findByToken(jwtToken.getToken())
                        .map(tokenInDb -> !tokenInDb.isExpired() && !tokenInDb.isRevoked()))
                .map(JwtToken::cloneAuthenticated)
                .switchIfEmpty(Mono.error(new JwtAuthenticationException("Invalid token")));
    }
}
