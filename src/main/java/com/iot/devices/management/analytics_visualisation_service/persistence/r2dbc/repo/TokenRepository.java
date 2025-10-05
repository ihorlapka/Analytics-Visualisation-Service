package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.Token;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TokenRepository extends R2dbcRepository<Token, UUID> {

    Mono<Token> findByToken(String token);
}
