package com.example.metatry.Repositories;

import com.example.metatry.Models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    boolean existsByExternalCommentId(String externalCommentId);

    Long countByPostId(Long postId);

}