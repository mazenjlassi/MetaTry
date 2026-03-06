package com.example.metatry.Services;

import com.example.metatry.Config.TokenConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class FacebookService {

    private final RestTemplate restTemplate;
    private final CloudinaryService cloudinaryService;
    private final Supplier<String> tokenSupplier;
    private final TokenConfig.TokenProvider tokenProvider;

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v19.0/";
    private static final String PAGE_ID = "968174046384507";

    public FacebookService(RestTemplate restTemplate,
                           CloudinaryService cloudinaryService,
                           Supplier<String> tokenSupplier,
                           TokenConfig.TokenProvider tokenProvider) {
        this.restTemplate = restTemplate;
        this.cloudinaryService = cloudinaryService;
        this.tokenSupplier = tokenSupplier;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Poster un message texte sur Facebook
     */
    public Map<String, Object> postText(String message) {
        return executeWithTokenRefresh(() -> {
            String url = GRAPH_API_URL + PAGE_ID + "/feed";

            Map<String, String> body = new HashMap<>();
            body.put("message", message);
            body.put("access_token", tokenSupplier.get());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            return Map.of("success", true, "postId", response.getBody().get("id"));
        });
    }

    /**
     * Poster une photo locale sur Facebook
     */
    public Map<String, Object> postLocalPhoto(MultipartFile file, String caption) {
        return executeWithTokenRefresh(() -> {
            try {
                String imageUrl = cloudinaryService.uploadImage(file);
                return executeFacebookPost(imageUrl, caption);
            } catch (Exception e) {
                return Map.of("success", false, "error", e.getMessage());
            }
        });
    }

    /**
     * Poster une photo via URL sur Facebook
     */
    public Map<String, Object> postPhotoFromUrl(String imageUrl, String caption) {
        return executeWithTokenRefresh(() -> executeFacebookPost(imageUrl, caption));
    }

    /**
     * Récupérer les infos de la page Facebook
     */
    public Map<String, Object> getPageInfo() {
        return executeWithTokenRefresh(() -> {
            String url = GRAPH_API_URL + PAGE_ID
                    + "?fields=id,name,about,fan_count&access_token="
                    + tokenSupplier.get();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        });
    }

    /**
     * Méthode privée pour exécuter les posts Facebook
     */
    private Map<String, Object> executeFacebookPost(String mediaUrl, String caption) {
        String url = GRAPH_API_URL + PAGE_ID + "/photos";

        Map<String, String> body = new HashMap<>();
        body.put("url", mediaUrl);
        body.put("caption", caption != null ? caption : "");
        body.put("access_token", tokenSupplier.get());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class
        );
        return Map.of("success", true, "postId", response.getBody().get("id"));
    }

    /**
     * Méthode utilitaire pour gérer le rafraîchissement automatique des tokens
     */
    private Map<String, Object> executeWithTokenRefresh(Operation operation) {
        try {
            return operation.execute();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 403 || e.getStatusCode().value() == 401) {
                System.out.println("🔄 Token expiré, rafraîchissement et nouvelle tentative...");
                tokenProvider.refresh();
                return operation.execute();
            }
            return Map.of("success", false, "error", e.getResponseBodyAsString());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @FunctionalInterface
    private interface Operation {
        Map<String, Object> execute();
    }
}