package com.example.metatry.Services;

import com.example.metatry.Config.CloudflareConfig;
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

    private final CloudinaryService cloudinaryService;
    private final CloudflareConfig cloudflareConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAndUploadImage(String prompt) {

        try {

            String url =
                    "https://api.cloudflare.com/client/v4/accounts/"
                            + cloudflareConfig.getAccountId()
                            + "/ai/run/@cf/stabilityai/stable-diffusion-xl-base-1.0";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cloudflareConfig.getApiToken());

            Map<String, Object> body = new HashMap<>();
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            byte[].class
                    );

            byte[] imageBytes = response.getBody();

            return cloudinaryService.uploadImageBytes(imageBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error generating AI image: " + e.getMessage());
        }
    }

    public PostImage generateImageForPost(Post post){

        ImageSize size = ImageSize.SQUARE; // choose default format

        String prompt = buildPrompt(post, size);

        String imageUrl = generateAndUploadImage(prompt);

        return PostImage.builder()
                .imageUrl(imageUrl)
                .imagePrompt(prompt)
                .size(size)
                .post(post)
                .selected(true)
                .build();
    }

    private PostImage createImage(Post post, ImageSize size){

        String prompt = buildPrompt(post, size);

        String imageUrl = generateAndUploadImage(prompt);

        return PostImage.builder()
                .imageUrl(imageUrl)
                .imagePrompt(prompt)
                .size(size)
                .post(post)
                .selected(false)
                .build();
    }

    private String buildPrompt(Post post, ImageSize size){

        String ratio = switch (size){

            case SQUARE -> "square composition for Instagram";

            case LANDSCAPE -> "wide landscape composition for LinkedIn or Facebook";

            case PORTRAIT -> "vertical mobile composition";
        };

        return "futuristic AI business technology illustration,generate it as a marketing team , it should be realistic "
                + ratio
                + ", modern digital art, vibrant colors, marketing style";
    }
}
