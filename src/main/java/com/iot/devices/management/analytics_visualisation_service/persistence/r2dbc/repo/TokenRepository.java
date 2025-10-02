package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.Token;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TokenRepository extends R2dbcRepository<Token, UUID> {

    @Query("SELECT * FROM token WHERE user_id = :userId AND (expired = FALSE OR revoked = FALSE)")
    Flux<Token> findAllValidTokenByUser(@Param("userId") UUID userId);

    Mono<Token> findByToken(String token);

    @Modifying
    @Query("DELETE FROM token WHERE user.id = :userId")
    int removeAllByUserId(@Param("userId") UUID userId);
}
