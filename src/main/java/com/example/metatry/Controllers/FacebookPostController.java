package com.example.metatry.Controllers;

import com.example.metatry.Services.FacebookService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/facebook")
public class FacebookPostController {

    private final FacebookService facebookService;

    public FacebookPostController(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    /**
     * 1. Poster un message texte sur Facebook
     * POST /api/facebook/post/text
     */
    @PostMapping("/post/text")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postText(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le message ne peut pas être vide"));
        }

        Map<String, Object> result = facebookService.postText(message);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * 2. Poster une photo via URL sur Facebook
     * POST /api/facebook/post/photo/url
     */
    @PostMapping("/post/photo/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postPhotoFromUrl(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("imageUrl");
        String caption = request.get("caption");

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'URL de l'image est requis"));
        }

        Map<String, Object> result = facebookService.postPhotoFromUrl(imageUrl, caption);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * 3. Poster une photo locale sur Facebook (via multipart)
     * POST /api/facebook/post/photo/local
     */
    @PostMapping(value = "/post/photo/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postLocalPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) {

        // Vérifications
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier est vide"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit être une image"));
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB max
            return ResponseEntity.badRequest().body(Map.of("error", "L'image ne doit pas dépasser 10MB"));
        }

        Map<String, Object> result = facebookService.postLocalPhoto(file, caption);

        return (boolean) result.get("success") ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    /**
     * 4. Récupérer les informations de la page Facebook
     * GET /api/facebook/page-info
     */
    @GetMapping("/page-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPageInfo() {
        Map<String, Object> result = facebookService.getPageInfo();
        return ResponseEntity.ok(result);
    }

    /**
     * 5. Tester la connexion Facebook
     * GET /api/facebook/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        try {
            Map<String, Object> pageInfo = facebookService.getPageInfo();

            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            response.put("service", "Facebook API");
            response.put("pageId", "968174046384507");
            response.put("pageName", "MazenJlassi");
            response.put("status", "✅ Connecté");
            response.put("pageInfo", pageInfo);
            response.put("tokenAutoRefresh", "✅ Actif (rafraîchissement toutes les 50 minutes)");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "❌ Erreur",
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 6. Health check
     * GET /api/facebook/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Facebook API",
                "timestamp", System.currentTimeMillis(),
                "tokenAutoRefresh", "Actif"
        ));
    }

    /**
     * 7. Documentation des endpoints
     * GET /api/facebook
     */
    @GetMapping
    public ResponseEntity<?> getEndpoints() {
        Map<String, Object> docs = new HashMap<>();
        docs.put("service", "Facebook API");
        docs.put("baseUrl", "/api/facebook");
        docs.put("tokenAutoRefresh", "✅ Les tokens sont rafraîchis automatiquement toutes les 50 minutes");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /post/text", "Publier un message texte (JSON: {\"message\": \"...\"})");
        endpoints.put("POST /post/photo/url", "Publier une photo via URL (JSON: {\"imageUrl\": \"...\", \"caption\": \"...\"})");
        endpoints.put("POST /post/photo/local", "Publier une photo locale (multipart/form-data avec file et caption)");
        endpoints.put("GET /page-info", "Récupérer les infos de la page");
        endpoints.put("GET /test", "Tester la connexion");
        endpoints.put("GET /health", "Health check");

        docs.put("endpoints", endpoints);

        return ResponseEntity.ok(docs);
    }
}