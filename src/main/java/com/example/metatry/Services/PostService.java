package com.example.metatry.Services;

import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post updatePost(Long id, UpdatePostRequest request){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if(request.getContent() != null)
            post.setContent(request.getContent());

        if(request.getHashtags() != null)
            post.setHashtags(request.getHashtags());

        if(request.getPlatform() != null)
            post.setPlatform(request.getPlatform());

        if(request.getImageUrl() != null)
            post.setImageUrl(request.getImageUrl());

        if(request.getVideoUrl() != null)
            post.setVideoUrl(request.getVideoUrl());

        if(request.getApproved() != null)
            post.setApproved(request.getApproved());

        if(request.getScheduledAt() != null)
            post.setScheduledAt(request.getScheduledAt());

        return postRepository.save(post);
    }
}