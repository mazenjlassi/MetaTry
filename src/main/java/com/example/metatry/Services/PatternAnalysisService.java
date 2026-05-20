package com.example.metatry.Services;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.DTOs.PatternResponse;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ContentPatternRepository;
import com.example.metatry.Repositories.ScrapedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatternAnalysisService {

    private final ContentPatternRepository contentPatternRepository;
    private final ScrapedPostRepository scrapedPostRepository;
    private final GeminiService geminiService;

    public PatternResponse analyzePattern(PatternAnalysisRequest request) {
        try {
            String topic = request.getTopic();
            String platform = request.getPlatform() != null ? request.getPlatform() : "linkedin";
            int minPosts = request.getMinPostsRequired() != null ? request.getMinPostsRequired() : 5;

            List<ScrapedPost> posts;
            if (request.getCompanyName() != null && !request.getCompanyName().isEmpty()) {
                posts = scrapedPostRepository.findByCompanyNameAndPlatform(request.getCompanyName(), platform);
            } else {
                posts = scrapedPostRepository.findByPlatform(platform);
            }

            if (posts.size() < minPosts) {
                return PatternResponse.builder()
                    .status("error")
                    .message("Not enough posts. Found " + posts.size() + ", need at least " + minPosts)
                    .build();
            }

            String postsText = posts.stream()
                .map(p -> "- " + p.getPostText())
                .collect(Collectors.joining("\n"));

            String prompt = buildAnalysisPrompt(topic, platform, postsText);

            String aiResponse = geminiService.generate(prompt);

            ContentPattern pattern = parseAndSavePattern(topic, platform, aiResponse, posts.size());

            return PatternResponse.builder()
                .id(pattern.getId())
                .topic(pattern.getTopic())
                .platform(pattern.getPlatform())
                .postFrequency(pattern.getPostFrequency())
                .contentLength(pattern.getContentLength())
                .mediaType(pattern.getMediaType())
                .hashtagCount(pattern.getHashtagCount())
                .timingPattern(pattern.getTimingPattern())
                .tone(pattern.getTone())
                .ctaStyle(pattern.getCtaStyle())
                .totalPostsAnalyzed(pattern.getTotalPostsAnalyzed())
                .extractedAt(pattern.getExtractedAt())
                .status("success")
                .message("Pattern extracted from " + posts.size() + " posts")
                .build();

        } catch (Exception e) {
            return PatternResponse.builder()
                .status("error")
                .message("Analysis failed: " + e.getMessage())
                .build();
        }
    }

    public List<ContentPattern> getAllPatterns() {
        return contentPatternRepository.findAll();
    }

    public ContentPattern getPatternByTopic(String topic) {
        return contentPatternRepository.findByTopic(topic).orElse(null);
    }

    public ContentPattern getPatternByTopicAndPlatform(String topic, String platform) {
        return contentPatternRepository.findByTopicAndPlatform(topic, platform).orElse(null);
    }

    private String buildAnalysisPrompt(String topic, String platform, String postsText) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following social media posts from a ").append(topic).append(" company on ").append(platform).append(" and extract content patterns.\n\n");
        prompt.append("Posts:\n").append(postsText).append("\n\n");
        prompt.append("Return a JSON with these exact fields (no markdown, just plain JSON):\n");
        prompt.append("{\n");
        prompt.append("  \"postFrequency\": \"e.g., 3x/week, daily, weekly\",\n");
        prompt.append("  \"contentLength\": \"e.g., 150-300 chars, under 100, 500+\",\n");
        prompt.append("  \"mediaType\": \"e.g., 80% images, 20% videos, mostly text\",\n");
        prompt.append("  \"hashtagCount\": \"e.g., 3-5 per post, none, 10+\",\n");
        prompt.append("  \"timingPattern\": \"e.g., Tuesday/Thursday 9-11am, morning posts, evening posts\",\n");
        prompt.append("  \"tone\": \"e.g., Technical/educational, casual/friendly, formal/professional\",\n");
        prompt.append("  \"ctaStyle\": \"e.g., Links to articles, questions to engage, calls to action\"\n");
        prompt.append("}");
        return prompt.toString();
    }

    private ContentPattern parseAndSavePattern(String topic, String platform, String aiResponse, int postsCount) {
        ContentPattern pattern = ContentPattern.builder()
            .topic(topic)
            .platform(platform)
            .totalPostsAnalyzed(postsCount)
            .build();

        try {
            if (aiResponse.contains("postFrequency")) {
                pattern.setPostFrequency(truncate(extractJsonValue(aiResponse, "postFrequency"), 100));
                pattern.setContentLength(truncate(extractJsonValue(aiResponse, "contentLength"), 100));
                pattern.setMediaType(truncate(extractJsonValue(aiResponse, "mediaType"), 100));
                pattern.setHashtagCount(truncate(extractJsonValue(aiResponse, "hashtagCount"), 100));
                pattern.setTimingPattern(truncate(extractJsonValue(aiResponse, "timingPattern"), 200));
                pattern.setTone(truncate(extractJsonValue(aiResponse, "tone"), 255));
                pattern.setCtaStyle(truncate(extractJsonValue(aiResponse, "ctaStyle"), 500));
                pattern.setAiAnalysisRaw(truncate(aiResponse, 5000));
            }
        } catch (Exception ignored) {}

        return contentPatternRepository.save(pattern);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() > maxLength ? value.substring(0, maxLength - 3) + "..." : value;
    }

    private String extractJsonValue(String json, String key) {
        try {
            String[] keys = {key, key.replace("Case", "_case"), key.replace("_", "")};
            for (String k : keys) {
                String pattern = "\"" + k + "\"\\s*:\\s*\"([^\"]+)\"";
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher m = p.matcher(json);
                if (m.find()) {
                    return m.group(1);
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}