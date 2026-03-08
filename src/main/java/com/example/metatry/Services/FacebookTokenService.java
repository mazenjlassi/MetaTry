package com.example.metatry.Services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class FacebookTokenService {

    private final RestTemplate restTemplate;

    @Value("${facebook.page-id}")
    private String pageId;

    @Value("${facebook.long-lived-token}")
    private String longLivedToken;

    private String currentPageToken;

    private boolean tokenValid = false;

    public FacebookTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Run once when Spring finishes loading the bean
     */
    @PostConstruct
    public void init() {
        refreshPageToken();
    }

    /**
     * Refresh token every 50 minutes
     */
    @Scheduled(fixedRate = 3000000)
    public void refreshPageToken() {

        try {

            System.out.println("🔄 [FacebookTokenService] Refreshing page token...");

            String url = UriComponentsBuilder
                    .fromUriString("https://graph.facebook.com/v19.0/me/accounts")
                    .queryParam("access_token", longLivedToken.trim())
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Facebook returned empty response");
            }

            List<Map<String, Object>> accounts =
                    (List<Map<String, Object>>) response.getBody().get("data");

            if (accounts == null || accounts.isEmpty()) {
                throw new RuntimeException("No pages found for this token");
            }

            for (Map<String, Object> account : accounts) {

                if (pageId.equals(account.get("id"))) {

                    currentPageToken = (String) account.get("access_token");

                    tokenValid = true;

                    System.out.println("✅ [FacebookTokenService] Page token refreshed successfully");

                    return;
                }
            }

            System.out.println("⚠️ [FacebookTokenService] Page not found in account list");

            tokenValid = false;

        } catch (Exception e) {

            tokenValid = false;

            System.err.println("❌ [FacebookTokenService] Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Return valid token for posting
     */
    public String getPageToken() {

        if (!tokenValid || currentPageToken == null) {
            refreshPageToken();
        }

        return currentPageToken;
    }

    /**
     * Check token state
     */
    public boolean isTokenValid() {
        return tokenValid;
    }

}