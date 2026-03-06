package com.example.metatry.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FacebookTokenService {

    private final RestTemplate restTemplate;

    @Value("${facebook.app-id}")
    private String appId;

    @Value("${facebook.app-secret}")
    private String appSecret;

    @Value("${facebook.page-id}")
    private String pageId;

    @Value("${facebook.long-lived-token}")
    private String longLivedToken; // C'est ton User Token (60 jours)

    private String currentPageToken;
    private long pageTokenExpiryTime;
    private boolean tokenValid = false;

    public FacebookTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // Au démarrage, on récupère directement le token de page
        refreshPageToken();
    }

    /**
     * Rafraîchit le token de page toutes les 50 minutes
     */
    @Scheduled(fixedRate = 3000000) // 50 minutes
    public void refreshPageToken() {
        try {
            System.out.println("🔄 [TokenService] Récupération du token de page...");

            // Utiliser le long-lived token (User Token) pour obtenir le token de page
            String url = "https://graph.facebook.com/v19.0/me/accounts?access_token=" + longLivedToken;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("data")) {
                var accounts = (java.util.List<Map<String, Object>>) response.getBody().get("data");
                for (var account : accounts) {
                    if (pageId.equals(account.get("id"))) {
                        this.currentPageToken = (String) account.get("access_token");
                        this.pageTokenExpiryTime = System.currentTimeMillis() + 3300000; // 55 minutes
                        this.tokenValid = true;
                        System.out.println("✅ [TokenService] Token de page obtenu avec succès");
                        return;
                    }
                }
                System.out.println("⚠️ [TokenService] Page non trouvée dans la liste");
            }
        } catch (Exception e) {
            this.tokenValid = false;
            System.err.println("❌ [TokenService] Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retourne le token de page actuel, le rafraîchit si nécessaire
     */
    public String getCurrentPageToken() {
        if (currentPageToken == null || System.currentTimeMillis() > pageTokenExpiryTime - 300000) {
            refreshPageToken();
        }
        return currentPageToken;
    }

    /**
     * Vérifie si le token est valide
     */
    public boolean isTokenValid() {
        return tokenValid && currentPageToken != null &&
                System.currentTimeMillis() < pageTokenExpiryTime;
    }

    /**
     * Force le rafraîchissement du token
     */
    public void forceRefresh() {
        refreshPageToken();
    }

    // ⚠️ On supprime la méthode refreshLongLivedToken car on n'en a pas besoin
    // Le long-lived token (60 jours) on le garde tel quel dans les propriétés
}