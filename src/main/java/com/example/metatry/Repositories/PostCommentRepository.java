package com.example.metatry.Repositories;

import com.example.metatry.Models.PostComment;
import com.example.metatry.Models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPost(Post post);

    List<PostComment> findBySentiment(String sentiment);

}