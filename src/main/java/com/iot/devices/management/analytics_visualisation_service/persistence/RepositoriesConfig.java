package com.iot.devices.management.analytics_visualisation_service.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.iot.devices.management.analytics_visualisation_service.persistence.mongo.repo")
@EnableR2dbcRepositories(basePackages = "com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.repo")
public class RepositoriesConfig {
}
