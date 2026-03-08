package com.example.metatry.Services;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialPublisherService {

    private final PostRepository postRepository;

    private final FacebookService facebookService;
    private final InstagramService instagramService;
    private final LinkedInService linkedInService;

    /*
     public void publishPost(Long postId){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if(!post.getApproved()){
            throw new RuntimeException("Post must be approved before publishing");
        }

        String message = post.getContent();

        if(post.getHashtags() != null){
            message += "\n\n" + post.getHashtags();
        }

        for(PlatformType platform : post.getPlatforms()){

            switch(platform){

                case FACEBOOK -> facebookService.postText(message);

                case INSTAGRAM -> instagramService.postPhotoFromUrl(
                        post.getImageUrl(),
                        message
                );

                case LINKEDIN -> linkedInService.postText(message);

            }
        }

        post.setPublishedAt(java.time.LocalDateTime.now());

        postRepository.save(post);
    }

     */
}