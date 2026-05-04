package com.example.metatry.Services;

import com.example.metatry.DTOs.AiGeneratedContent;
import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiContentService {

    private final PromptBuilderService promptBuilderService;
    private final GeminiService geminiService;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    private final ObjectMapper objectMapper;

    // ================= MAIN ENTRY =================

    public List<Post> generatePostsWithCampaign(
            String topic,
            int postNumber,
            Campaign campaign,
            String insights,
            String conclusion
    ) {

        // ✅ SAFE FALLBACKS
        String safeInsights = (insights == null || insights.isBlank())
                ? "No insights available"
                : insights;

        String safeConclusion = (conclusion == null || conclusion.isBlank())
                ? "Focus on engagement, clarity, and value"
                : conclusion;

        List<Post> allPosts = new ArrayList<>();

        for (int i = 0; i < postNumber; i++) {
            List<Post> batch = generateSingleBatch(topic, campaign, safeInsights, safeConclusion);
            allPosts.addAll(batch);
        }

        return allPosts;
    }

    // ================= GENERATE ONE BATCH =================

    private List<Post> generateSingleBatch(
            String topic,
            Campaign campaign,
            String insights,
            String conclusion
    ) {

        try {

            // 🔥 NEW PROMPT (INSIGHTS + STRATEGY)
            String prompt = promptBuilderService.buildPrompt(topic, insights, conclusion);

            String aiText = geminiService.generate(prompt);

            AiGeneratedContent aiContent =
                    objectMapper.readValue(aiText, AiGeneratedContent.class);

            List<Post> postsToSave = new ArrayList<>();

            postsToSave.add(createPost(
                    aiContent.getLinkedinTitle(),
                    aiContent.getLinkedinPost(),
                    aiContent.getLinkedinHashtags(),
                    PlatformType.LINKEDIN,
                    campaign
            ));

            postsToSave.add(createPost(
                    aiContent.getInstagramTitle(),
                    aiContent.getInstagramPost(),
                    aiContent.getInstagramHashtags(),
                    PlatformType.INSTAGRAM,
                    campaign
            ));

            postsToSave.add(createPost(
                    aiContent.getFacebookTitle(),
                    aiContent.getFacebookPost(),
                    aiContent.getFacebookHashtags(),
                    PlatformType.FACEBOOK,
                    campaign
            ));

            List<Post> savedPosts = postRepository.saveAll(postsToSave);

            // ================= IMAGES =================

            for (Post post : savedPosts) {

                ImageSize size = switch (post.getPlatform()) {
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

    // ================= CREATE POST =================

    private Post createPost(
            String title,
            String content,
            List<String> hashtags,
            PlatformType platform,
            Campaign campaign
    ) {

        return Post.builder()
                .title(title)
                .content(content)
                .hashtags(hashtags != null ? String.join(",", hashtags) : "")
                .platform(platform)
                .generatedByAI(true)
                .approved(false)
                .status(PostStatus.DRAFT)
                .campaign(campaign)
                .permanent(false)
                .link("https://3lm-solutions2.odoo.com/contactus")
                .build();
    }
}