package com.example.metatry.Config;

import com.example.metatry.Services.FacebookTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class TokenConfig {

    /**
     * Supplier that always returns a valid page token
     */
    @Bean
    public Supplier<String> pageTokenSupplier(FacebookTokenService tokenService) {

        return () -> {

            String token = tokenService.getPageToken();

            if (token == null) {
                throw new RuntimeException("❌ Facebook page token not available");
            }

            return token;
        };
    }

    /**
     * Advanced token provider
     */
    @Bean
    public TokenProvider tokenProvider(FacebookTokenService tokenService) {

        return new TokenProvider() {

            @Override
            public String getToken() {
                return tokenService.getPageToken();
            }

            @Override
            public String getTokenWithRefresh() {

                tokenService.refreshPageToken();

                return tokenService.getPageToken();
            }

            @Override
            public boolean isTokenValid() {
                return tokenService.isTokenValid();
            }

            @Override
            public void refresh() {
                tokenService.refreshPageToken();
            }
        };
    }

    public interface TokenProvider {

        String getToken();

        String getTokenWithRefresh();

        boolean isTokenValid();

        void refresh();
    }
}