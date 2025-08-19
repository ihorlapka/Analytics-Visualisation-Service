package com.iot.devices.management.analytics_visualisation_service;

import org.springframework.boot.SpringApplication;

public class TestAnalyticsVisualisationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(AnalyticsVisualisationServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
