package com.example.metatry.Services;

import com.example.metatry.Config.StabilityConfig;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiImageService {

    private final CloudinaryService cloudinaryService;
    private final StabilityConfig stabilityConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Generate AI image and upload to Cloudinary
     */
    public String generateAndUploadImage(String prompt) {

        try {

            String url = "https://api.stability.ai/v2beta/stable-image/generate/sd3";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(stabilityConfig.getApiKey());
            headers.set("Accept", "image/*");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("prompt", prompt);
            body.add("output_format", "png");

            HttpEntity<MultiValueMap<String, Object>> request =
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

    /**
     * Generate 3 AI images for a post
     */
    public List<PostImage> generateImagesForPost(Post post){

        List<PostImage> images = new ArrayList<>();

        images.add(createImage(post, ImageSize.SQUARE));
        images.add(createImage(post, ImageSize.LANDSCAPE));
        images.add(createImage(post, ImageSize.PORTRAIT));

        return images;
    }

    /**
     * Create one image with a specific format
     */
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

    /**
     * Build optimized prompt depending on image size
     */
    private String buildPrompt(Post post, ImageSize size){

        String ratio = switch (size){

            case SQUARE -> "square composition for Instagram";

            case LANDSCAPE -> "wide landscape composition for LinkedIn or Facebook";

            case PORTRAIT -> "vertical mobile composition";
        };

        return "futuristic AI business technology illustration, "
                + ratio
                + ", modern digital art, vibrant colors, marketing style";
    }
}