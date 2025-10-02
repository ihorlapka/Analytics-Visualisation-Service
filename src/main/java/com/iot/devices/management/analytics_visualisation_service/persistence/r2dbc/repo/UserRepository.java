package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {

    Mono<User> findByUsername(String username);
}
