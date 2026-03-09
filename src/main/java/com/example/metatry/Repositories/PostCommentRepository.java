package com.example.metatry.Repositories;

import com.example.metatry.Models.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostId(Long postId);

}