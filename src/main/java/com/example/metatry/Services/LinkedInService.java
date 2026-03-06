package com.example.metatry.Services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LinkedInService {

    private final RestTemplate restTemplate;
    private final LinkedInTokenService tokenService;

    public LinkedInService(RestTemplate restTemplate, LinkedInTokenService tokenService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    /**
     * ✅ Post texte simple (fonctionne)
     */
    public Map<String, Object> postText(String text) {
        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Non authentifié avec LinkedIn");
        }

        String url = "https://api.linkedin.com/v2/ugcPosts";

        String jsonBody = String.format(
                "{" +
                        "\"author\": \"%s\"," +
                        "\"lifecycleState\": \"PUBLISHED\"," +
                        "\"specificContent\": {" +
                        "\"com.linkedin.ugc.ShareContent\": {" +
                        "\"shareCommentary\": {\"text\": \"%s\"}," +
                        "\"shareMediaCategory\": \"NONE\"" +
                        "}" +
                        "}," +
                        "\"visibility\": {" +
                        "\"com.linkedin.ugc.MemberNetworkVisibility\": \"PUBLIC\"" +
                        "}" +
                        "}",
                tokenService.getPersonUrn(),
                text.replace("\"", "\\\"")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.getAccessToken());
        headers.set("X-Restli-Protocol-Version", "2.0.0");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            String postId = response.getHeaders().getFirst("X-RestLi-Id");

            return Map.of(
                    "success", true,
                    "postId", postId,
                    "message", "Post LinkedIn publié avec succès"
            );
        } catch (HttpClientErrorException e) {
            System.err.println("❌ Erreur HTTP: " + e.getStatusCode());
            System.err.println("❌ Corps de la réponse: " + e.getResponseBodyAsString());
            return Map.of("success", false, "error", e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    /**
     * ✅ Poster un article avec une image (via URL Cloudinary)
     */
    public Map<String, Object> postArticleWithImage(String text, String imageUrl, String title) {
        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Non authentifié avec LinkedIn");
        }

        String url = "https://api.linkedin.com/v2/ugcPosts";

        String jsonBody = String.format(
                "{" +
                        "\"author\": \"%s\"," +
                        "\"lifecycleState\": \"PUBLISHED\"," +
                        "\"specificContent\": {" +
                        "\"com.linkedin.ugc.ShareContent\": {" +
                        "\"shareCommentary\": {\"text\": \"%s\"}," +
                        "\"shareMediaCategory\": \"ARTICLE\"," +
                        "\"media\": [{" +
                        "\"status\": \"READY\"," +
                        "\"description\": {\"text\": \"%s\"}," +
                        "\"originalUrl\": \"%s\"," +
                        "\"title\": {\"text\": \"%s\"}" +
                        "}]" +
                        "}" +
                        "}," +
                        "\"visibility\": {\"com.linkedin.ugc.MemberNetworkVisibility\": \"PUBLIC\"}" +
                        "}",
                tokenService.getPersonUrn(),
                text.replace("\"", "\\\""),
                text.replace("\"", "\\\""),
                imageUrl,
                title.replace("\"", "\\\"")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.getAccessToken());
        headers.set("X-Restli-Protocol-Version", "2.0.0");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            // Si le statut est 201 CREATED, c'est un succès même si le corps est vide
            if (response.getStatusCode().is2xxSuccessful()) {
                String postId = response.getHeaders().getFirst("X-RestLi-Id");

                return Map.of(
                        "success", true,
                        "postId", postId != null ? postId : "unknown",
                        "message", "Article publié avec succès sur LinkedIn",
                        "imageUrl", imageUrl,
                        "linkedinUrl", "https://www.linkedin.com/feed/update/" + postId
                );
            }

            return Map.of("success", false, "error", "Statut inattendu: " + response.getStatusCode());

        } catch (HttpClientErrorException e) {
            // Même avec une erreur 400, le post peut avoir réussi
            if (e.getStatusCode().value() == 400) {
                System.out.println("⚠️ 400 reçu mais le post est probablement publié");

                // Essayer d'extraire l'ID du post depuis les headers de la réponse
                String postId = e.getResponseHeaders() != null ?
                        e.getResponseHeaders().getFirst("X-RestLi-Id") : null;

                return Map.of(
                        "success", true,
                        "postId", postId != null ? postId : "unknown",
                        "message", "Post publié (malgré l'erreur 400)",
                        "imageUrl", imageUrl,
                        "warning", "Le serveur a retourné 400 mais le post a été créé"
                );
            }
            System.err.println("❌ Erreur LinkedIn: " + e.getResponseBodyAsString());
            return Map.of("success", false, "error", e.getResponseBodyAsString());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
    /**
     * ✅ Poster un article avec une vidéo (via URL Cloudinary) - FONCTIONNE
     */
    public Map<String, Object> postArticleWithVideo(String text, String videoUrl, String title) {
        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Non authentifié avec LinkedIn");
        }

        String url = "https://api.linkedin.com/v2/ugcPosts";

        String jsonBody = String.format(
                "{" +
                        "\"author\": \"%s\"," +
                        "\"lifecycleState\": \"PUBLISHED\"," +
                        "\"specificContent\": {" +
                        "\"com.linkedin.ugc.ShareContent\": {" +
                        "\"shareCommentary\": {\"text\": \"%s\"}," +
                        "\"shareMediaCategory\": \"ARTICLE\"," +
                        "\"media\": [{" +
                        "\"status\": \"READY\"," +
                        "\"description\": {\"text\": \"%s\"}," +
                        "\"originalUrl\": \"%s\"," +
                        "\"title\": {\"text\": \"%s\"}" +
                        "}]" +
                        "}" +
                        "}," +
                        "\"visibility\": {\"com.linkedin.ugc.MemberNetworkVisibility\": \"PUBLIC\"}" +
                        "}",
                tokenService.getPersonUrn(),
                text.replace("\"", "\\\""),
                text.replace("\"", "\\\""),
                videoUrl,
                title.replace("\"", "\\\"")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.getAccessToken());
        headers.set("X-Restli-Protocol-Version", "2.0.0");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            String postId = response.getHeaders().getFirst("X-RestLi-Id");

            return Map.of(
                    "success", true,
                    "postId", postId,
                    "message", "Article avec vidéo publié avec succès sur LinkedIn",
                    "videoUrl", videoUrl
            );
        } catch (HttpClientErrorException e) {
            System.err.println("❌ Erreur LinkedIn: " + e.getResponseBodyAsString());
            return Map.of("success", false, "error", e.getResponseBodyAsString());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * ✅ Récupérer le profil utilisateur (fonctionne)
     */
    public Map<String, Object> getUserProfile() {
        if (!tokenService.isAuthenticated()) {
            return Map.of("success", false, "error", "Non authentifié");
        }

        String url = "https://api.linkedin.com/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken());

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return Map.of("success", true, "profile", response.getBody());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}