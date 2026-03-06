package com.example.metatry.Controllers;

import com.example.metatry.Models.Post;
import com.example.metatry.Services.AiContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiContentService aiContentService;


    @PostMapping("/generate")
    public Post generatePost(@RequestParam String topic){

        return aiContentService.generatePost(topic);

    }
}