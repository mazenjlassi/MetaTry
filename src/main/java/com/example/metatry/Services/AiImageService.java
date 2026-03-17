package com.example.metatry.Services;

import com.example.metatry.Config.CloudflareConfig;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
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
    private  final PostImageRepository postImageRepository ;

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
            headers.setAccept(List.of(MediaType.IMAGE_PNG, MediaType.APPLICATION_OCTET_STREAM));

            Map<String, Object> body = new HashMap<>();
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    byte[].class
            );

            // Check Cloudflare response
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Cloudflare AI returned status: " + response.getStatusCode());
            }

            byte[] imageBytes = response.getBody();

            // Validate image
            if (imageBytes == null || imageBytes.length == 0) {
                throw new RuntimeException("AI returned empty image");
            }

            String imageUrl = cloudinaryService.uploadImageBytes(imageBytes);

            // Validate upload
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new RuntimeException("Cloudinary upload failed");
            }

            return imageUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error generating AI image: " + e.getMessage(), e);
        }
    }
    public PostImage generateImageForPost(Post post){

        PostImage image = post.getImage();

        if(image == null){
            throw new RuntimeException("Post has no image prompt");
        }

        String prompt = image.getImagePrompt();

        if(prompt == null || prompt.isBlank()){
            throw new RuntimeException("Image prompt is missing");
        }

        // Generate AI image
        String imageUrl = generateAndUploadImage(prompt);

        if(imageUrl == null || imageUrl.isBlank()){
            throw new RuntimeException("AI image generation failed");
        }

        // Update existing image row
        image.setImageUrl(imageUrl);
        image.setSelected(true);

        return postImageRepository.save(image);
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

        // If the post already has an AI-generated image prompt, use it
        if(post.getImage() != null &&
                post.getImage().getImagePrompt() != null &&
                !post.getImage().getImagePrompt().isBlank()) {

            return post.getImage().getImagePrompt();
        }

        // Otherwise build a fallback prompt
        String ratio = switch (size){

            case SQUARE -> "square composition for Instagram";

            case LANDSCAPE -> "wide landscape composition for LinkedIn or Facebook";

            case PORTRAIT -> "vertical mobile composition";
        };

        return "realistic professional business technology scene, "
                + ratio
                + ", modern office environment, cinematic lighting, photorealistic style, marketing campaign visual";
    }
}
