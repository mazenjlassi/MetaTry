package com.example.metatry.Services;

import com.example.metatry.Config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;

    public String generate(String prompt){

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + geminiConfig.getApiKey();

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        String rawText = extractText(response.getBody());

        return cleanJson(rawText);
    }

    private String extractText(Map<String, Object> response){

        try {
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e){
            throw new RuntimeException("Failed to extract Gemini response: " + response);
        }
    }

    // 🔥 CLEAN JSON SAFELY
    private String cleanJson(String text){

        if(text == null) return "";

        // remove markdown
        text = text.replace("```json", "");
        text = text.replace("```", "");

        text = text.trim();

        // extract JSON block
        int firstBrace = text.indexOf("{");
        int lastBrace = text.lastIndexOf("}");

        if(firstBrace != -1 && lastBrace != -1){
            text = text.substring(firstBrace, lastBrace + 1);
        }

        return text;
    }
}