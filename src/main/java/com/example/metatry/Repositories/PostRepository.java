package com.example.metatry.Repositories;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByApprovedFalse();

    List<Post> findByApprovedTrue();

    List<Post> findByStatus(PostStatus status);

    List<Post> findByPlatform(PlatformType platform);

    long countByStatus(PostStatus status);

    long countByApprovedTrue();

    long countByPlatform(PlatformType platform);
    List<Post> findByApprovedTrueAndStatusAndScheduledAtBefore(
            PostStatus status,
            LocalDateTime time
    );

}