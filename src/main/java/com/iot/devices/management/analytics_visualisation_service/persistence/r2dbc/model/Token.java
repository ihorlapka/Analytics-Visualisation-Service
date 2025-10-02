package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tokens")
@ToString
public class Token {
    @Id
    private UUID id;
    public String token;
    public boolean revoked;
    public boolean expired;
    public boolean refresh;
    public UUID userId;
}
