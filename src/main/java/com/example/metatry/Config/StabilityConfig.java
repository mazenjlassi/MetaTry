package com.example.metatry.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StabilityConfig {

    @Value("${stability.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}