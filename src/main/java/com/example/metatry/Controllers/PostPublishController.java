package com.example.metatry.Controllers;

import com.example.metatry.Services.SocialPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostPublishController {

   /* private final SocialPublisherService publisherService;

    @PostMapping("/publish/{id}")
    public String publishPost(@PathVariable Long id){

        publisherService.publishPost(id);

        return "Post published successfully";

    }

    */
}