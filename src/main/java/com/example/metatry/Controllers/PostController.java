package com.example.metatry.Controllers;

import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;

    @GetMapping("/pending")
    public List<Post> pendingPosts(){
        return postRepository.findByApprovedFalse();
    }

    @PutMapping("/{id}")
    public Post updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request){

        return postService.updatePost(id, request);
    }
}