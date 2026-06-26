package com.example.metatry.Services;

import com.example.metatry.Enums.ImageSize;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialPublisherService {

    private final InstagramService instagramService;
    private final FacebookService facebookService;
    private final LinkedInService linkedInService;
    private final EmailService emailService;
    private final PostRepository postRepository;
    public Post publishPost(Post post){

        if(!Boolean.TRUE.equals(post.getApproved())){
            throw new RuntimeException("Post must be approved before publishing");
        }

        String caption = buildCaption(post);
        List<PostImage> images = selectImages(post);

        switch (post.getPlatform()) {
            case INSTAGRAM -> publishInstagram(post, images, caption);
            case FACEBOOK -> publishFacebook(post, images, caption);
            case LINKEDIN -> publishLinkedIn(post, images, caption);
        }

        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());

        if (post.getPlatformPostId() != null
                && post.getStatus() == PostStatus.PUBLISHED
                && !post.isNotificationSent()) {

            try {
                emailService.sendPostPublishedEmail(post);
                post.setNotificationSent(true);
            } catch (Exception e) {
                System.out.println("Email failed: " + e.getMessage());
            }
        }

        return postRepository.save(post);
    }

    private String buildCaption(Post post){
        String caption = post.getContent();

        if (post.getLink() != null && !post.getLink().isBlank()) {
            caption += "\n\n" + post.getLink();
        }

        if (post.getHashtags() != null && !post.getHashtags().isBlank()) {
            caption += "\n\n" + post.getHashtags();
        }

        return caption;
    }

    private List<PostImage> selectImages(Post post){
        List<PostImage> images = post.getImages();
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .filter(i -> Boolean.TRUE.equals(i.getSelected()))
                .sorted((a, b) -> {
                    int oa = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    int ob = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return Integer.compare(oa, ob);
                })
                .toList();
    }

    private void publishInstagram(Post post, List<PostImage> images, String caption){

        Map<String,Object> response;

        String videoUrl = post.getVideoUrl();

        if(videoUrl != null && !videoUrl.isBlank()){
            response = instagramService.postVideoFromUrl(videoUrl, caption);
        } else if (images.size() > 1) {
            List<String> urls = images.stream().map(PostImage::getImageUrl).toList();
            response = instagramService.postCarousel(urls, caption);
        } else if (!images.isEmpty() && images.get(0).getImageUrl() != null){
            response = instagramService.postPhotoFromUrl(
                    images.get(0).getImageUrl(),
                    caption
            );
        } else {
            System.out.println("Skipping Instagram post " + post.getId() + ": no image or video");
            return;
        }

        if(Boolean.TRUE.equals(response.get("success"))){
            post.setPlatformPostId((String) response.get("mediaId"));
        } else {
            System.out.println("Instagram publish failed for post " + post.getId() + ": " + response.get("error"));
        }
    }

    private void publishFacebook(Post post, List<PostImage> images, String caption){

        Map<String,Object> response;

        String videoUrl = post.getVideoUrl();

        if(videoUrl != null && !videoUrl.isBlank()){
            response = facebookService.postVideoFromUrl(videoUrl, caption);
        } else if (images.size() > 1) {
            List<String> urls = images.stream().map(PostImage::getImageUrl).toList();
            response = facebookService.postMultiplePhotos(urls, caption);
        } else if (!images.isEmpty() && images.get(0).getImageUrl() != null){
            response = facebookService.postPhotoFromUrl(
                    images.get(0).getImageUrl(),
                    caption
            );
        } else {
            response = facebookService.postText(caption);
        }

        if(response != null && response.get("id") != null){
            post.setPlatformPostId((String) response.get("id"));
        }
    }

    private void publishLinkedIn(Post post, List<PostImage> images, String caption){

        Map<String,Object> response;

        PostImage firstImage = images.isEmpty() ? null : images.get(0);

        if(firstImage != null && firstImage.getImageUrl() != null){

            response = linkedInService.postArticleWithImage(
                    caption,
                    firstImage.getImageUrl(),
                    "AI Generated Post"
            );

        } else {

            response = linkedInService.postText(caption);

        }

        if(Boolean.TRUE.equals(response.get("success"))){
            post.setPlatformPostId((String) response.get("postId"));
        }
    }
}