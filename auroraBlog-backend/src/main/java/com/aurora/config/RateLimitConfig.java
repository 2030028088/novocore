package com.aurora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    public static final int MAX_REQUESTS_PER_MINUTE = 60;
    public static final int MAX_REQUESTS_PER_HOUR = 500;

    @Bean
    public ConcurrentHashMap<String, long[]> rateLimitMap() {
        return new ConcurrentHashMap<>();
    }
}