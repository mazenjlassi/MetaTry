package com.example.metatry.Services;

import com.example.metatry.DTOs.PostDto;
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

    public PostDto mapToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .hashtags(post.getHashtags())
                .platform(post.getPlatform() != null ? post.getPlatform().name() : null)
                .scheduledAt(post.getScheduledAt())
                .publishedAt(post.getPublishedAt())
                .permanent(post.isPermanent())
                .link(post.getLink())
                .likes(post.getLikes())
                .commentsCount(post.getCommentsCount())
                .shares(post.getShares())
                .campaignId(post.getCampaign() != null ? post.getCampaign().getId() : null)
                .campaignName(post.getCampaign() != null ? post.getCampaign().getName() : null)
                .imageUrl(post.getImage() != null ? post.getImage().getImageUrl() : null)
                .status(post.getStatus().name())

                .build();
    }

    public PostDto getPostById(Long id) {
        Post post = postRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return mapToDto(post);
    }

    // ================= UPDATE POST =================
    public Post updatePost(Long id, UpdatePostRequest request){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 🚨 BLOCK update if published (unless permanent)
        if (post.getStatus() == PostStatus.PUBLISHED && !post.isPermanent()) {
            throw new RuntimeException("Cannot update a published non-permanent post");
        }

        if(request.getTitle() != null)
            post.setTitle(request.getTitle());

        if(request.getContent() != null)
            post.setContent(request.getContent());

        if(request.getHashtags() != null)
            post.setHashtags(request.getHashtags().replace(" ", ""));

        if(request.getVideoUrl() != null)
            post.setVideoUrl(request.getVideoUrl());

        if(request.getApproved() != null)
            post.setApproved(request.getApproved());

        if(request.getScheduledAt() != null)
            post.setScheduledAt(request.getScheduledAt());

        if(request.getPermanent() != null)
            post.setPermanent(request.getPermanent());

        if(request.getLink() != null)
            post.setLink(request.getLink());

        // 🔥 restrict platform change
        if(request.getPlatform() != null && post.getStatus() == PostStatus.DRAFT)
            post.setPlatform(request.getPlatform());

        // 🔥 update image if exists
        if(request.getImageUrl() != null && post.getImage() != null)
            post.getImage().setImageUrl(request.getImageUrl());

        // 🔥 update status logic
        if (post.getScheduledAt() != null && post.getStatus() != PostStatus.PUBLISHED) {
            post.setStatus(PostStatus.SCHEDULED);
        }

        return postRepository.save(post);
    }


    // ================= DELETE =================

    public void deletePost(Long id){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        postRepository.delete(post);
    }

    // ================= BASIC GET =================

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

    // 🔥 NEW: Permanent posts
    public List<Post> getPermanentPosts(){
        return postRepository.findByPermanentTrue();
    }

    // ================= STATS =================

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

    // ================= SCHEDULER =================

    public List<Post> getScheduledPosts(){

        return postRepository.findByStatus(PostStatus.SCHEDULED);
    }

    // ================= CLEAN IMAGES =================

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

    // ================= DASHBOARD =================

    public List<Post> getLatestPublishedPosts(int limit) {

        return postRepository.findByStatus(
                PostStatus.PUBLISHED,
                org.springframework.data.domain.PageRequest.of(
                        0,
                        limit,
                        org.springframework.data.domain.Sort.by("publishedAt").descending()
                )
        ).getContent();
    }

    public List<Post> getTopPosts(int limit) {

        return postRepository.findByStatus(
                PostStatus.PUBLISHED,
                org.springframework.data.domain.PageRequest.of(
                        0,
                        limit,
                        org.springframework.data.domain.Sort.by("likes").descending()
                )
        ).getContent();
    }

    // ================= POSTS DISPLAY =================

    //  By campaign
    public List<Post> getPostsByCampaign(Long campaignId) {
        return postRepository.findByCampaignId(campaignId);
    }

    //  Campaign + Status
    public List<Post> getCampaignPostsByStatus(Long campaignId, PostStatus status) {
        return postRepository.findByCampaignIdAndStatus(campaignId, status);
    }
}