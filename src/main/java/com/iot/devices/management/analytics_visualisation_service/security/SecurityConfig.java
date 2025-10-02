package com.iot.devices.management.analytics_visualisation_service.security;

import com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;

import static com.iot.devices.management.analytics_visualisation_service.persistence.enums.UserRole.*;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final String[] WHITE_LIST = {
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveAuthenticationManager reactiveAuthenticationManager,
                                                         ServerAuthenticationConverter authenticationConverter) {
        final AuthenticationWebFilter authFilter = new AuthenticationWebFilter(reactiveAuthenticationManager);
        authFilter.setServerAuthenticationConverter(authenticationConverter);
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(WHITE_LIST).permitAll()
                        .pathMatchers("/api/v1/devices/**").hasAnyRole(ADMIN.name(), MANAGER.name(), SUPER_ADMIN.name())
                        .pathMatchers("/api/v1/devicesPerManufacturer").hasAnyRole(ADMIN.name(), MANAGER.name(), SUPER_ADMIN.name())
                        .anyExchange()
                        .authenticated()
                )
                .authenticationManager(reactiveAuthenticationManager)
                .addFilterBefore(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userService) {
        return username -> userService.findByUsername(username)
                .map(user -> (UserDetails) user)
                .onErrorMap(e -> new UsernameNotFoundException(e.getMessage()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
