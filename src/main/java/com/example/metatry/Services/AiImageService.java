package com.example.metatry.Services;

import com.example.metatry.Config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiImageService {

    private final GeminiConfig geminiConfig;
    private final CloudinaryService cloudinaryService;

    public String generateAndUploadImage(String prompt) {

        try {

            RestTemplate restTemplate = new RestTemplate();

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key="
                            + geminiConfig.getApiKey();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String,Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            HttpEntity<Map<String,Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            Map candidate = (Map) candidates.get(0);

            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");

            Map imagePart = (Map) parts.get(0);
            Map inlineData = (Map) imagePart.get("inlineData");

            String base64Image = (String) inlineData.get("data");

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return cloudinaryService.uploadImageBytes(imageBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error generating AI image", e);
        }
    }
}