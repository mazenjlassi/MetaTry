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
public class InstagramService {

    private final RestTemplate restTemplate;
    private final CloudinaryService cloudinaryService;
    private final Supplier<String> tokenSupplier;
    private final TokenConfig.TokenProvider tokenProvider;

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v19.0/";
    private static final String INSTAGRAM_BUSINESS_ID = "17841445365706357";

    public InstagramService(RestTemplate restTemplate,
                            CloudinaryService cloudinaryService,
                            Supplier<String> tokenSupplier,
                            TokenConfig.TokenProvider tokenProvider) {
        this.restTemplate = restTemplate;
        this.cloudinaryService = cloudinaryService;
        this.tokenSupplier = tokenSupplier;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Poster une photo locale sur Instagram
     */
    public Map<String, Object> postLocalPhoto(MultipartFile file, String caption) {
        return executeWithTokenRefresh(() -> {
            try {
                String imageUrl = cloudinaryService.uploadImage(file);
                return executeInstagramPost(imageUrl, caption, "IMAGE");
            } catch (Exception e) {
                return Map.of("success", false, "error", e.getMessage());
            }
        });
    }

    /**
     * Poster une photo via URL sur Instagram
     */
    public Map<String, Object> postPhotoFromUrl(String imageUrl, String caption) {
        return executeWithTokenRefresh(() ->
                executeInstagramPost(imageUrl, caption, "IMAGE")
        );
    }

    /**
     * Poster une vidéo locale sur Instagram
     */
    public Map<String, Object> postLocalVideo(MultipartFile file, String caption) {
        return executeWithTokenRefresh(() -> {
            try {
                if (file.getSize() > 100 * 1024 * 1024) {
                    return Map.of("success", false, "error", "La vidéo ne doit pas dépasser 100MB");
                }

                Map<String, Object> options = new HashMap<>();
                options.put("folder", "metatry/instagram/videos");
                options.put("resource_type", "video");

                Map<String, Object> uploadResult = cloudinaryService.uploadWithOptions(file, options);
                String videoUrl = (String) uploadResult.get("secure_url");

                return executeInstagramPost(videoUrl, caption, "VIDEO");
            } catch (Exception e) {
                return Map.of("success", false, "error", e.getMessage());
            }
        });
    }

    /**
     * Poster une vidéo via URL sur Instagram
     */
    public Map<String, Object> postVideoFromUrl(String videoUrl, String caption) {
        return executeWithTokenRefresh(() ->
                executeInstagramPost(videoUrl, caption, "VIDEO")
        );
    }

    /**
     * Récupérer les infos du compte Instagram
     */
    public Map<String, Object> getAccountInfo() {
        return executeWithTokenRefresh(() -> {
            String url = GRAPH_API_URL + INSTAGRAM_BUSINESS_ID
                    + "?fields=id,username,name,profile_picture_url,followers_count,media_count&access_token="
                    + tokenSupplier.get();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> result = new HashMap<>(response.getBody());
            result.put("success", true);
            return result;
        });
    }

    /**
     * Tester la connexion Instagram
     */
    public Map<String, Object> testConnection() {
        return executeWithTokenRefresh(() -> {
            String url = GRAPH_API_URL + INSTAGRAM_BUSINESS_ID
                    + "?fields=id,username&access_token="
                    + tokenSupplier.get();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            return Map.of(
                    "success", true,
                    "message", "✅ Connexion Instagram OK",
                    "account", Map.of(
                            "id", response.getBody().get("id"),
                            "username", response.getBody().get("username")
                    )
            );
        });
    }

    /**
     * Exécuter le post Instagram (2 étapes)
     */
    private Map<String, Object> executeInstagramPost(String mediaUrl, String caption, String mediaType) {
        try {
            // Étape 1: Créer le container
            String createUrl = GRAPH_API_URL + INSTAGRAM_BUSINESS_ID + "/media";

            Map<String, String> body = new HashMap<>();
            if ("VIDEO".equals(mediaType)) {
                body.put("media_type", "VIDEO");
                body.put("video_url", mediaUrl);
            } else {
                body.put("image_url", mediaUrl);
            }
            body.put("caption", caption != null ? caption : "");
            body.put("access_token", tokenSupplier.get());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> createResponse = restTemplate.exchange(
                    createUrl, HttpMethod.POST, entity, Map.class
            );

            String containerId = (String) createResponse.getBody().get("id");
            Thread.sleep(5000);

            // Étape 2: Publier
            String publishUrl = GRAPH_API_URL + INSTAGRAM_BUSINESS_ID + "/media_publish";

            Map<String, String> publishBody = new HashMap<>();
            publishBody.put("creation_id", containerId);
            publishBody.put("access_token", tokenSupplier.get());

            HttpEntity<Map<String, String>> publishEntity = new HttpEntity<>(publishBody, headers);

            ResponseEntity<Map> publishResponse = restTemplate.exchange(
                    publishUrl, HttpMethod.POST, publishEntity, Map.class
            );

            return Map.of(
                    "success", true,
                    "mediaId", publishResponse.getBody().get("id"),
                    "message", mediaType + " publié avec succès sur Instagram !"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Map.of("success", false, "error", "Timeout pendant l'attente");
        }
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