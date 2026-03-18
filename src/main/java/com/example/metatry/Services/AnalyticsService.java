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
import java.util.Map;
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PostService postService;
    private final PostMetricRepository postMetricRepository;
    private final PostRepository postRepository;
    private final FacebookTokenService tokenService;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Main entry: Collect metrics for all published posts
     */
    public void collectMetricsForPublishedPosts(){

        List<Post> posts = postService.getPublishedPosts();

        for(Post post : posts){

            // Skip posts without platform ID
            if(post.getPlatformPostId() == null) continue;

            try {

                switch (post.getPlatform()){

                    case FACEBOOK -> fetchFacebookMetrics(post);

                    case INSTAGRAM -> fetchInstagramMetrics(post);

                    case LINKEDIN -> {
                        // Skip for now (API restricted)
                        continue;
                    }
                }

            } catch (Exception e){
                System.out.println("❌ Error fetching metrics for post " + post.getPlatformPostId());
                e.printStackTrace(); // important for debugging
            }
        }
    }

    /**
     * Fetch metrics from Facebook Page posts
     */
    private void fetchFacebookMetrics(Post post){

        String token = tokenService.getPageToken();

        String url = "https://graph.facebook.com/v19.0/" + post.getPlatformPostId() +
                "?fields=likes.summary(true),comments.summary(true),shares" +
                "&access_token=" + token;

        System.out.println("➡️ Facebook API call: " + post.getPlatformPostId());

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if(response == null) return;

        System.out.println("✅ FB Response: " + response);

        int likes = extractLikes(response);
        int comments = extractComments(response);
        int shares = extractShares(response);

        saveAndUpdate(post, likes, comments, shares, 0);
    }

    /**
     * Fetch metrics from Instagram media
     */
    private void fetchInstagramMetrics(Post post){

        String token = tokenService.getPageToken();

        String url = "https://graph.facebook.com/v19.0/" + post.getPlatformPostId() +
                "?fields=like_count,comments_count" +
                "&access_token=" + token;

        System.out.println("➡️ Instagram API call: " + post.getPlatformPostId());

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if(response == null) return;

        System.out.println("✅ IG Response: " + response);

        int likes = response.get("like_count") != null
                ? ((Number) response.get("like_count")).intValue()
                : 0;

        int comments = response.get("comments_count") != null
                ? ((Number) response.get("comments_count")).intValue()
                : 0;

        int shares = 0; // Instagram doesn't support shares

        saveAndUpdate(post, likes, comments, shares, 0);
    }

    /**
     * Extract Facebook likes safely
     */
    private int extractLikes(Map<String, Object> response){
        try{
            Map<String, Object> likes = (Map<String, Object>) response.get("likes");
            if(likes == null) return 0;

            Map<String, Object> summary = (Map<String, Object>) likes.get("summary");
            if(summary == null) return 0;

            return ((Number) summary.get("total_count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    /**
     * Extract Facebook comments safely
     */
    private int extractComments(Map<String, Object> response){
        try{
            Map<String, Object> comments = (Map<String, Object>) response.get("comments");
            if(comments == null) return 0;

            Map<String, Object> summary = (Map<String, Object>) comments.get("summary");
            if(summary == null) return 0;

            return ((Number) summary.get("total_count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    /**
     * Extract Facebook shares safely
     */
    private int extractShares(Map<String, Object> response){
        try{
            Map<String, Object> shares = (Map<String, Object>) response.get("shares");
            if(shares == null) return 0;

            return ((Number) shares.get("count")).intValue();
        } catch(Exception e){
            return 0;
        }
    }

    /**
     * Save metrics history + update latest snapshot in Post
     */
    private void saveAndUpdate(Post post, int likes, int comments, int shares, int impressions){

        // Save metrics history
        PostMetric metric = PostMetric.builder()
                .post(post)
                .likes(likes)
                .comments(comments)
                .shares(shares)
                .impressions(impressions)
                .collectedAt(LocalDateTime.now())
                .build();

        postMetricRepository.save(metric);

        // Update latest snapshot in Post
        post.setLikes(likes);
        post.setCommentsCount(comments);
        post.setShares(shares);
        post.setImpressions(impressions);

        // Simple engagement formula
        double engagement = impressions > 0
                ? (double)(likes + comments * 2 + shares * 3) / impressions
                : 0;

        post.setEngagementScore(engagement);

        postRepository.save(post);
    }
}