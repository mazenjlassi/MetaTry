package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PostService postService;
    private final PostMetricRepository postMetricRepository;
    private final PostRepository postRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${facebook.page-access-token}")
    private String token;

    @Value("${facebook.page-id}")
    private String pageId;

    public void collectMetricsForPublishedPosts(){

        List<Post> posts = postService.getPublishedPosts();

        for(Post post : posts){

            if(post.getPlatformPostId() == null) continue;

            try {

                switch (post.getPlatform()){

                    case FACEBOOK -> fetchFacebookMetrics(post);
                    case INSTAGRAM -> fetchInstagramMetrics(post);
                    case LINKEDIN -> { continue; }
                }

            } catch (Exception e){
                System.out.println("❌ Error fetching metrics for post " + post.getPlatformPostId());
                e.printStackTrace();
            }
        }
    }

    private void fetchFacebookMetrics(Post post){

        String postId = post.getPlatformPostId();

        // Fix postId format
        if(postId != null && !postId.contains("_")){
            postId = pageId + "_" + postId;
        }

        String url = "https://graph.facebook.com/v19.0/" + postId +
                "?fields=likes.summary(true),comments.summary(true),shares" +
                "&access_token=" + token;

        System.out.println("➡️ Facebook API call: " + postId);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if(response == null){
                System.out.println("❌ FB response is null");
                return;
            }

            int likes = extractLikes(response);
            int comments = extractComments(response);
            int shares = extractShares(response);

            saveAndUpdate(post, likes, comments, shares, 0);

        } catch (Exception e){
            System.out.println("❌ Facebook fetch failed for post: " + postId);
            e.printStackTrace();
        }
    }

    private void fetchInstagramMetrics(Post post){

        String url = "https://graph.facebook.com/v19.0/" + post.getPlatformPostId() +
                "?fields=like_count,comments_count" +
                "&access_token=" + token;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if(response == null) return;

        int likes = response.get("like_count") != null
                ? ((Number) response.get("like_count")).intValue()
                : 0;

        int comments = response.get("comments_count") != null
                ? ((Number) response.get("comments_count")).intValue()
                : 0;

        saveAndUpdate(post, likes, comments, 0, 0);
    }

    private int extractLikes(Map<String, Object> response){
        try{
            Map<String, Object> likes = (Map<String, Object>) response.get("likes");
            Map<String, Object> summary = (Map<String, Object>) likes.get("summary");
            return ((Number) summary.get("total_count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    private int extractComments(Map<String, Object> response){
        try{
            Map<String, Object> comments = (Map<String, Object>) response.get("comments");
            Map<String, Object> summary = (Map<String, Object>) comments.get("summary");
            return ((Number) summary.get("total_count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    private int extractShares(Map<String, Object> response){
        try{
            Map<String, Object> shares = (Map<String, Object>) response.get("shares");
            return ((Number) shares.get("count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }
    private void saveAndUpdate(Post post, int likes, int comments, int shares, int impressions){

        // ✅ 1. Save history (ALWAYS)
        PostMetric metric = PostMetric.builder()
                .post(post)
                .likes(likes)
                .comments(comments)
                .shares(shares)
                .impressions(impressions)
                .collectedAt(LocalDateTime.now())
                .build();

        postMetricRepository.save(metric);

        // ✅ 2. Update latest snapshot SAFELY (never decrease)
        int currentLikes = post.getLikes() != null ? post.getLikes() : 0;
        int currentComments = post.getCommentsCount() != null ? post.getCommentsCount() : 0;
        int currentShares = post.getShares() != null ? post.getShares() : 0;
        int currentImpressions = post.getImpressions() != null ? post.getImpressions() : 0;

        post.setLikes(Math.max(currentLikes, likes));
        post.setCommentsCount(Math.max(currentComments, comments));
        post.setShares(Math.max(currentShares, shares));
        post.setImpressions(Math.max(currentImpressions, impressions));

        // ✅ 3. Calculate engagement (fallback if impressions = 0)
        int safeImpressions = post.getImpressions() > 0
                ? post.getImpressions()
                : (post.getLikes() + post.getCommentsCount() + post.getShares());

        double engagement = safeImpressions > 0
                ? (double)(post.getLikes() + post.getCommentsCount() * 2 + post.getShares() * 3)
                / safeImpressions
                : 0;

        post.setEngagementScore(engagement);

        // ✅ 4. Save post
        postRepository.save(post);
    }
}