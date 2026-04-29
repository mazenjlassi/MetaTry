package com.example.metatry.Repositories;

import com.example.metatry.Models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostId(Long postId);

    List<PostComment> findByPostCampaignId(Long campaignId);

    List<PostComment> findByPostIdAndSentiment(Long postId, String sentiment);

    long countByPostId(Long postId); // 🔥 FIX

    boolean existsByExternalCommentId(String externalCommentId);
}