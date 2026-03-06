package com.example.metatry.Repositories;

import com.example.metatry.Models.Post;
import com.example.metatry.Enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Posts ready to publish
    List<Post> findByStatusAndScheduledAtBefore(
            PostStatus status,
            LocalDateTime time
    );

    // Published posts
    List<Post> findByStatus(PostStatus status);

    // Top performing posts
    List<Post> findTop5ByOrderByEngagementScoreDesc();

    List<Post> findByApprovedFalse();

    // Recent posts
    List<Post> findTop10ByOrderByPublishedAtDesc();
}