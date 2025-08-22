package com.reliaquest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mock-api")
public class MockApiProperties {
    private String baseUrl = "http://localhost:8112/api/v1/employee";
    private int connectionTimeout = 5000;
    private int readTimeout = 10000;
    private RetryConfig retry = new RetryConfig();

    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long initialDelay = 1000;
        private long maxDelay = 5000;
        private double multiplier = 2.0;
    }
}