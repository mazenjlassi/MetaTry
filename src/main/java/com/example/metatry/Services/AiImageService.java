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
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class AiImageService {

    private final CloudinaryService cloudinaryService;
    private final CloudflareConfig cloudflareConfig;
    private final PostImageRepository postImageRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // ================= CLOUDFARE + CLOUDINARY =================

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

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Cloudflare AI returned status: " + response.getStatusCode()
                );
            }

            byte[] imageBytes = response.getBody();

            if (imageBytes == null || imageBytes.length == 0) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "AI returned empty image"
                );
            }

            String imageUrl = cloudinaryService.uploadImageBytes(imageBytes);

            if (imageUrl == null || imageUrl.isBlank()) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Cloudinary upload failed"
                );
            }

            return imageUrl;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Error generating AI image: " + e.getMessage()
            );
        }
    }

    // ================= MAIN LOGIC =================

    public PostImage generateImageForPost(Post post){

        if (post == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Post not found");
        }

        PostImage image = post.getImage();

        // ✅ CASE 1: No image exists → create one
        if (image == null) {
            image = createImage(post, ImageSize.SQUARE);
            image.setSelected(true);
            return postImageRepository.save(image);
        }

        String prompt = image.getImagePrompt();

        // ✅ CASE 2: Prompt missing → build one
        if (prompt == null || prompt.isBlank()) {
            prompt = buildPrompt(post, ImageSize.SQUARE);
            image.setImagePrompt(prompt);
        }

        // ✅ Generate AI image
        String imageUrl = generateAndUploadImage(prompt);

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "AI image generation failed"
            );
        }

        image.setImageUrl(imageUrl);
        image.setSelected(true);

        return postImageRepository.save(image);
    }

    // ================= CREATE IMAGE =================

    private PostImage createImage(Post post, ImageSize size){

        String prompt = buildPrompt(post, size);

        String imageUrl = generateAndUploadImage(prompt);

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Failed to generate image"
            );
        }

        return PostImage.builder()
                .imageUrl(imageUrl)
                .imagePrompt(prompt)
                .size(size)
                .post(post)
                .selected(false)
                .build();
    }

    // ================= PROMPT BUILDER =================

    private String buildPrompt(Post post, ImageSize size){

        if (post.getImage() != null &&
                post.getImage().getImagePrompt() != null &&
                !post.getImage().getImagePrompt().isBlank()) {

            return post.getImage().getImagePrompt();
        }

        String ratio = switch (size){
            case SQUARE -> "square composition for Instagram";
            case LANDSCAPE -> "wide landscape composition for LinkedIn or Facebook";
            case PORTRAIT -> "vertical mobile composition";
        };

        String base = "";

        if (post.getTitle() != null) {
            base += post.getTitle() + ", ";
        }

        if (post.getContent() != null) {
            base += post.getContent() + ", ";
        }

        return base +
                "realistic professional business technology scene, " +
                ratio +
                ", modern office environment, cinematic lighting, photorealistic style, marketing campaign visual";
    }
}