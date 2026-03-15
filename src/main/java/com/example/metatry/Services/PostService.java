package com.example.metatry.Services;

import com.example.metatry.DTOs.PostStatsResponse;
import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

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

        if(request.getPlatform() != null)
            post.setPlatform(request.getPlatform());

        if(request.getVideoUrl() != null)
            post.setVideoUrl(request.getVideoUrl());

        if(request.getApproved() != null)
            post.setApproved(request.getApproved());

        if(request.getScheduledAt() != null)
            post.setScheduledAt(request.getScheduledAt());

        return postRepository.save(post);
    }
    public void deletePost(Long id){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postRepository.delete(post);
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





    public void cleanDuplicateImages() {

        List<PostImage> allImages = postImageRepository.findAll();

        Map<Long, PostImage> uniqueImages = new HashMap<>();

        for (PostImage image : allImages) {

            Long postId = image.getPost().getId();

            if (!uniqueImages.containsKey(postId)) {
                uniqueImages.put(postId, image);
            } else {
                postImageRepository.delete(image);
            }
        }
    }

}