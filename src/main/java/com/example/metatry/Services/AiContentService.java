package com.example.metatry.Services;

import com.example.metatry.Config.GeminiConfig;
import com.example.metatry.DTOs.AiGeneratedContent;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiContentService {

    private final GeminiConfig geminiConfig;
    private final PromptBuilderService promptBuilderService;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Post> generatePosts(String topic){

        try {

            String prompt = promptBuilderService.buildPrompt(topic);

            RestTemplate restTemplate = new RestTemplate();

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
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

            Map part = (Map) parts.get(0);

            String aiText = (String) part.get("text");

            aiText = cleanJson(aiText);

            AiGeneratedContent aiContent =
                    objectMapper.readValue(aiText, AiGeneratedContent.class);

            // Create posts
            Post linkedinPost = createPost(
                    aiContent.getLinkedinPost(),
                    aiContent.getLinkedinHashtags(),
                    PlatformType.LINKEDIN
            );

            Post instagramPost = createPost(
                    aiContent.getInstagramPost(),
                    aiContent.getInstagramHashtags(),
                    PlatformType.INSTAGRAM
            );

            Post facebookPost = createPost(
                    aiContent.getFacebookPost(),
                    aiContent.getFacebookHashtags(),
                    PlatformType.FACEBOOK
            );

            List<Post> savedPosts = postRepository.saveAll(
                    List.of(linkedinPost, instagramPost, facebookPost)
            );

            // Create image prompts for each post
            for(Post post : savedPosts){

                ImageSize size = switch(post.getPlatform()){
                    case INSTAGRAM -> ImageSize.SQUARE;
                    case LINKEDIN, FACEBOOK -> ImageSize.LANDSCAPE;
                };

                PostImage image = PostImage.builder()
                        .imagePrompt(aiContent.getImagePrompt())
                        .size(size)
                        .post(post)
                        .selected(false)
                        .build();

                postImageRepository.save(image);
            }

            return savedPosts;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException("Error generating AI posts: " + e.getMessage());

        }
    }

    private Post createPost(String content, List<String> hashtags, PlatformType platform){

        return Post.builder()
                .content(content)
                .hashtags(String.join(",", hashtags))
                .platform(platform)
                .generatedByAI(true)
                .approved(false)
                .status(PostStatus.DRAFT)
                .build();
    }

    private String cleanJson(String text){

        text = text.replace("```json", "");
        text = text.replace("```", "");

        return text.trim();
    }
}