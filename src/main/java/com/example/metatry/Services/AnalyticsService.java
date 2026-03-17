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

    private final RestTemplate restTemplate = new RestTemplate();

    private final FacebookTokenService tokenService;

    public void collectMetricsForPublishedPosts(){

        List<Post> posts = postService.getPublishedPosts();

        for(Post post : posts){

            // skip if no platform ID
            if(post.getPlatformPostId() == null) continue;

            try {

                switch (post.getPlatform()){

                    case FACEBOOK, INSTAGRAM -> fetchMetaMetrics(post);

                    case LINKEDIN -> {
                        // skip for now
                        continue;
                    }
                }

            } catch (Exception e){
                System.out.println("Error fetching metrics for post " + post.getId());
            }
        }
    }

    private void fetchMetaMetrics(Post post){

        String postId = post.getPlatformPostId();

        String url = "https://graph.facebook.com/v19.0/" + postId +
                "?fields=likes.summary(true),comments.summary(true),shares" +
                "&access_token=" + tokenService;

        Map response = restTemplate.getForObject(url, Map.class);

        int likes = extractLikes(response);
        int comments = extractComments(response);
        int shares = extractShares(response);
        int impressions = 0; // optional (requires insights API)

        saveAndUpdate(post, likes, comments, shares, impressions);
    }

    private int extractLikes(Map response){
        try{
            Map likes = (Map) response.get("likes");
            Map summary = (Map) likes.get("summary");
            return (Integer) summary.get("total_count");
        } catch(Exception e){
            return 0;
        }
    }

    private int extractComments(Map response){
        try{
            Map comments = (Map) response.get("comments");
            Map summary = (Map) comments.get("summary");
            return (Integer) summary.get("total_count");
        } catch(Exception e){
            return 0;
        }
    }

    private int extractShares(Map response){
        try{
            Map shares = (Map) response.get("shares");
            return shares == null ? 0 : (Integer) shares.get("count");
        } catch(Exception e){
            return 0;
        }
    }

    private void saveAndUpdate(Post post, int likes, int comments, int shares, int impressions){

        // Save history
        PostMetric metric = PostMetric.builder()
                .post(post)
                .likes(likes)
                .comments(comments)
                .shares(shares)
                .impressions(impressions)
                .collectedAt(LocalDateTime.now())
                .build();

        postMetricRepository.save(metric);

        // Update latest snapshot
        post.setLikes(likes);
        post.setCommentsCount(comments);
        post.setShares(shares);
        post.setImpressions(impressions);

        double engagement = impressions > 0
                ? (double)(likes + comments * 2 + shares * 3) / impressions
                : 0;

        post.setEngagementScore(engagement);

        postRepository.save(post);
    }
}