package com.example.metatry.Services;

import com.example.metatry.Config.GeminiConfig;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiImageService {

    private final GeminiConfig geminiConfig;
    private final CloudinaryService cloudinaryService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Generate an image with Gemini and upload to Cloudinary
     */
    public String generateAndUploadImage(String prompt) {

        try {

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key="
                            + geminiConfig.getApiKey();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String,Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            HttpEntity<Map<String,Object>> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            List candidates = (List) response.getBody().get("candidates");

            Map candidate = (Map) candidates.get(0);

            Map content = (Map) candidate.get("content");

            List parts = (List) content.get("parts");

            Map inlineData = null;

            for(Object p : parts){
                Map part = (Map) p;
                if(part.containsKey("inlineData")){
                    inlineData = (Map) part.get("inlineData");
                    break;
                }
            }

            if(inlineData == null){
                throw new RuntimeException("No image returned from Gemini");
            }

            String base64Image = (String) inlineData.get("data");

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            return cloudinaryService.uploadImageBytes(imageBytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating AI image: " + e.getMessage());
        }
    }

    /**
     * Generate multiple images for a post
     */
    public List<PostImage> generateImagesForPost(Post post){

        List<PostImage> images = new ArrayList<>();

        images.add(createImage(post, ImageSize.SQUARE));
        images.add(createImage(post, ImageSize.LANDSCAPE));
        images.add(createImage(post, ImageSize.PORTRAIT));

        return images;
    }

    private String buildPromptFromPost(Post post){

        return "Marketing image for social media post: "
                + post.getContent()
                + ". Style: modern, professional, high quality illustration.";
    }

    /**
     * Create a single image adapted to the size
     */
    private PostImage createImage(Post post, ImageSize size){

        String prompt = buildSizedPrompt(buildPromptFromPost(post), size);

        String imageUrl = generateAndUploadImage(prompt);

        return PostImage.builder()
                .imageUrl(imageUrl)
                .imagePrompt(prompt)
                .size(size)
                .post(post)
                .selected(false)
                .build();
    }

    /**
     * Adapt prompt for each platform format
     */
    private String buildSizedPrompt(String basePrompt, ImageSize size){

        String formatInstruction = switch(size){

            case SQUARE ->
                    "square composition, optimized for Instagram, centered subject";

            case LANDSCAPE ->
                    "wide landscape composition, optimized for LinkedIn or Facebook";

            case PORTRAIT ->
                    "vertical portrait composition, optimized for mobile feeds";
        };

        return basePrompt + ", " + formatInstruction + ", high quality marketing illustration";
    }
}