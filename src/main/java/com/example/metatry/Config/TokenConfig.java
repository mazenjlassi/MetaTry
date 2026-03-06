package com.example.metatry.Config;

import com.example.metatry.Services.FacebookTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class TokenConfig {

    /**
     * Fournit un Supplier qui donne toujours un token valide
     * Utilisé par FacebookService et InstagramService
     */
    @Bean
    public Supplier<String> pageTokenSupplier(FacebookTokenService tokenService) {
        return () -> {
            String token = tokenService.getCurrentPageToken();
            if (token == null) {
                throw new RuntimeException("❌ Token de page non disponible");
            }
            return token;
        };
    }

    /**
     * Provider avec plus de fonctionnalités
     */
    @Bean
    public TokenProvider tokenProvider(FacebookTokenService tokenService) {
        return new TokenProvider() {
            @Override
            public String getToken() {
                return tokenService.getCurrentPageToken();
            }

            @Override
            public String getTokenWithRefresh() {
                tokenService.forceRefresh();
                return tokenService.getCurrentPageToken();
            }

            @Override
            public boolean isTokenValid() {
                return tokenService.isTokenValid();
            }

            @Override
            public void refresh() {
                tokenService.forceRefresh();
            }
        };
    }

    /**
     * Interface pour le provider de token
     */
    public interface TokenProvider {
        String getToken();
        String getTokenWithRefresh();
        boolean isTokenValid();
        void refresh();
    }
}