package com.example.metatry.Services;

import com.example.metatry.DTOs.AnalyticsRequest;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostMetric;
import com.example.metatry.Repositories.PostMetricRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PostRepository postRepository;
    private final PostMetricRepository postMetricRepository;

    public void updateMetrics(AnalyticsRequest request){

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        /*
        Save historical metrics
         */
        PostMetric metric = PostMetric.builder()
                .post(post)
                .likes(request.getLikes())
                .comments(request.getComments())
                .shares(request.getShares())
                .impressions(request.getImpressions())
                .collectedAt(LocalDateTime.now())
                .build();

        postMetricRepository.save(metric);

        /*
        Update latest snapshot
         */
        post.setLikes(request.getLikes());
        post.setCommentsCount(request.getComments());
        post.setShares(request.getShares());
        post.setImpressions(request.getImpressions());

        /*
        Calculate engagement score
         */
        if(request.getImpressions() != null && request.getImpressions() > 0){

            double engagement =
                    (request.getLikes()
                            + request.getComments()
                            + request.getShares())
                            * 100.0
                            / request.getImpressions();

            post.setEngagementScore(engagement);
        }

        postRepository.save(post);
    }
}