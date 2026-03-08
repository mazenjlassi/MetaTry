package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialPublisherService {

    private final FacebookService facebookService;
    private final InstagramService instagramService;
    private final LinkedInService linkedInService;
    private final PostRepository postRepository;

    public Post publishPost(Post post) {

        if(!post.getApproved()){
            throw new RuntimeException("Post must be approved before publishing");
        }

        PlatformType platform = post.getPlatform();

        switch (platform){

            case FACEBOOK -> publishFacebook(post);

            case INSTAGRAM -> publishInstagram(post);

            case LINKEDIN -> publishLinkedin(post);
        }

        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    private void publishFacebook(Post post){

        Map<String,Object> result =
                facebookService.postText(post.getContent());

        System.out.println("Facebook publish result: " + result);
    }

    private void publishInstagram(Post post){

        if(post.getImageUrl() == null){
            throw new RuntimeException("Instagram requires an imageUrl");
        }

        Map<String,Object> result =
                instagramService.postPhotoFromUrl(
                        post.getImageUrl(),
                        post.getContent()
                );

        System.out.println("Instagram publish result: " + result);
    }

    private void publishLinkedin(Post post){

        Map<String,Object> result =
                linkedInService.postText(post.getContent());

        System.out.println("LinkedIn publish result: " + result);
    }

}