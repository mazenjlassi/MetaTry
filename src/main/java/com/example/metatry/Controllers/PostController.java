package com.example.metatry.Controllers;

import com.example.metatry.DTOs.PostStatsResponse;
import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request){

        postService.updatePost(id, request);

        return ResponseEntity.ok("Post updated");
    }

    @GetMapping
    public List<Post> getAllPosts(){
        return postService.getAllPosts();
    }

    // Get published posts
    @GetMapping("/published")
    public List<Post> getPublishedPosts(){
        return postService.getPublishedPosts();
    }

    // Get draft posts
    @GetMapping("/draft")
    public List<Post> getDraftPosts(){
        return postService.getDraftPosts();
    }

    // Get approved posts
    @GetMapping("/approved")
    public List<Post> getApprovedPosts(){
        return postService.getApprovedPosts();
    }

    // Get posts by platform
    @GetMapping("/platform/{platform}")
    public List<Post> getPostsByPlatform(@PathVariable PlatformType platform){
        return postService.getPostsByPlatform(platform);
    }

    @GetMapping("/stats")
    public PostStatsResponse getStats(){
        return postService.getStats();
    }
}