package com.example.metatry.Services;

import com.example.metatry.DTOs.PostStatsResponse;
import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> getAllPosts(){
        return postRepository.findAll();
    }

    public Post updatePost(Long id, UpdatePostRequest request){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if(request.getContent() != null)
            post.setContent(request.getContent());

        if(request.getHashtags() != null)
            post.setHashtags(request.getHashtags());

        if(request.getPlatform() != null){
            post.setPlatform(request.getPlatform());
        }

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

    public List<Post> getPublishedPosts(){
        return postRepository.findByStatus(PostStatus.PUBLISHED);
    }

    public List<Post> getDraftPosts(){
        return postRepository.findByStatus(PostStatus.DRAFT);
    }

    public List<Post> getApprovedPosts(){
        return postRepository.findByApprovedTrue();
    }

    public List<Post> getPostsByPlatform(PlatformType platform){
        return postRepository.findByPlatform(platform);
    }

    public PostStatsResponse getStats(){

        long total = postRepository.count();

        long published = postRepository.countByStatus(PostStatus.PUBLISHED);

        long draft = postRepository.countByStatus(PostStatus.DRAFT);

        long approved = postRepository.countByApprovedTrue();

        long facebook = postRepository.countByPlatform(PlatformType.FACEBOOK);

        long instagram = postRepository.countByPlatform(PlatformType.INSTAGRAM);

        long linkedin = postRepository.countByPlatform(PlatformType.LINKEDIN);

        return new PostStatsResponse(
                total,
                published,
                draft,
                approved,
                facebook,
                instagram,
                linkedin
        );

    }

    public List<Post> getScheduledPosts(){

        return postRepository
                .findByApprovedTrueAndStatusAndScheduledAtBefore(
                        PostStatus.DRAFT,
                        LocalDateTime.now()
                );
    }
}