package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {

    Mono<User> findByUsername(String username);

    @Query("""
            SELECT u.* FROM devices d
            JOIN users u ON d.owner_user_id = u.id
            WHERE d.id = :deviceId
            """)
    Mono<User> findByDeviceId(@Param("deviceId") UUID deviceId);
}
