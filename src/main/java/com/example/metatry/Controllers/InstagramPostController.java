package com.example.metatry.Controllers;

import com.example.metatry.Services.InstagramService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/instagram")
public class InstagramPostController {

    private final InstagramService instagramService;

    public InstagramPostController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    /**
     * 1. Poster une photo via URL sur Instagram
     * POST /api/instagram/post/photo/url
     */
    @PostMapping("/post/photo/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postPhotoFromUrl(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("imageUrl");
        String caption = request.get("caption");

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'URL de l'image est requis"));
        }

        Map<String, Object> result = instagramService.postPhotoFromUrl(imageUrl, caption);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * 2. Poster une photo locale sur Instagram
     * POST /api/instagram/post/photo/local
     */
    @PostMapping(value = "/post/photo/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postLocalPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier est vide"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une image"));
        }

        if (file.getSize() > 8 * 1024 * 1024) { // 8MB max pour Instagram
            return ResponseEntity.badRequest().body(Map.of("error", "L'image ne doit pas dépasser 8MB"));
        }

        Map<String, Object> result = instagramService.postLocalPhoto(file, caption);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * 3. Poster une vidéo via URL sur Instagram
     * POST /api/instagram/post/video/url
     */
    @PostMapping("/post/video/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postVideoFromUrl(@RequestBody Map<String, String> request) {
        String videoUrl = request.get("videoUrl");
        String caption = request.get("caption");

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'URL de la vidéo est requis"));
        }

        Map<String, Object> result = instagramService.postVideoFromUrl(videoUrl, caption);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * 4. Poster une vidéo locale sur Instagram
     * POST /api/instagram/post/video/local
     */
    @PostMapping(value = "/post/video/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postLocalVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier est vide"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une vidéo"));
        }

        Map<String, Object> result = instagramService.postLocalVideo(file, caption);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * 5. Récupérer les informations du compte Instagram
     * GET /api/instagram/account-info
     */
    @GetMapping("/account-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAccountInfo() {
        Map<String, Object> result = instagramService.getAccountInfo();
        return ResponseEntity.ok(result);
    }

    /**
     * 6. Tester la connexion Instagram
     * GET /api/instagram/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        Map<String, Object> result = instagramService.testConnection();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "Instagram API");
        response.put("instagramBusinessId", "17841445365706357");
        response.put("username", "try77074");
        response.put("connection", result);
        response.put("tokenAutoRefresh", "✅ Actif (rafraîchissement toutes les 50 minutes)");
        response.put("note", "Le token est automatiquement rafraîchi, plus besoin de le changer manuellement !");

        return ResponseEntity.ok(response);
    }

    /**
     * 7. Health check
     * GET /api/instagram/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Instagram API",
                "timestamp", System.currentTimeMillis(),
                "tokenAutoRefresh", "Actif"
        ));
    }

    /**
     * 8. Documentation des endpoints
     * GET /api/instagram
     */
    @GetMapping
    public ResponseEntity<?> getEndpoints() {
        Map<String, Object> docs = new HashMap<>();
        docs.put("service", "Instagram API");
        docs.put("baseUrl", "/api/instagram");
        docs.put("instagramBusinessId", "17841445365706357");
        docs.put("username", "try77074");
        docs.put("tokenAutoRefresh", "✅ Les tokens sont rafraîchis automatiquement toutes les 50 minutes");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /post/photo/url", "Publier une photo via URL (JSON: {\"imageUrl\": \"...\", \"caption\": \"...\"})");
        endpoints.put("POST /post/photo/local", "Publier une photo locale (multipart/form-data avec file et caption)");
        endpoints.put("POST /post/video/url", "Publier une vidéo via URL (JSON: {\"videoUrl\": \"...\", \"caption\": \"...\"})");
        endpoints.put("POST /post/video/local", "Publier une vidéo locale (multipart/form-data avec file et caption)");
        endpoints.put("GET /account-info", "Récupérer les infos du compte");
        endpoints.put("GET /test", "Tester la connexion");
        endpoints.put("GET /health", "Health check");

        docs.put("endpoints", endpoints);

        return ResponseEntity.ok(docs);
    }
}