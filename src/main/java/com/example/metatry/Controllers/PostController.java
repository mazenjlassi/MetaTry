package com.example.metatry.Controllers;

import com.example.metatry.DTOs.PostDto;
import com.example.metatry.DTOs.PostStatsResponse;
import com.example.metatry.DTOs.UpdatePostRequest;
import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.PostImageRepository;
import com.example.metatry.Repositories.PostRepository;
import com.example.metatry.Services.AiImageService;
import com.example.metatry.Services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;
    private final AiImageService aiImageService;
    private final PostImageRepository postImageRepository;

    // ================= BASIC =================

    @GetMapping
    public List<Post> getAllPosts(){
        return postService.getAllPosts();
    }


    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/drafts")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getDrafts() {
        return postService.getDraftPosts();
    }

    @GetMapping("/scheduled")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getScheduled() {
        return postService.getScheduledPosts();
    }

    @GetMapping("/published")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getPublished() {
        return postService.getPublishedPosts();
    }

    @GetMapping("/approved")
    public List<Post> getApprovedPosts(){
        return postService.getApprovedPosts();
    }

    @GetMapping("/platform/{platform}")
    public List<Post> getPostsByPlatform(@PathVariable PlatformType platform){
        return postService.getPostsByPlatform(platform);
    }

    // ================= CAMPAIGN =================

    @GetMapping("/campaign/{id}")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getByCampaign(@PathVariable Long id) {
        return postService.getPostsByCampaign(id);
    }

    @GetMapping("/campaign/{id}/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getCampaignPostsByStatus(
            @PathVariable Long id,
            @PathVariable PostStatus status) {

        return postService.getCampaignPostsByStatus(id, status);
    }

    // ================= UPDATE =================

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request){

        postService.updatePost(id, request);

        return ResponseEntity.ok(Map.of("message", "Post updated"));
    }

    // ================= DELETE =================

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable Long id){

        postService.deletePost(id);

        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    // ================= STATS =================

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public PostStatsResponse getStats(){
        return postService.getStats();
    }

    // ================= AI IMAGE =================

    @PostMapping("/{id}/generate-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostImage> generateImage(@PathVariable Long id){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostImage image = aiImageService.generateImageForPost(post);

        return ResponseEntity.ok(image);
    }

    // ================= CLEANUP =================

    @DeleteMapping("/cleanup-images")
    public ResponseEntity<?> cleanDuplicateImages(){

        postService.cleanDuplicateImages();

        return ResponseEntity.ok(Map.of("message", "Duplicate images removed"));
    }

    // ================= DASHBOARD =================

    @GetMapping("/latestPublished")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getLatestPublished(
            @RequestParam(defaultValue = "15") int limit
    ){
        return postService.getLatestPublishedPosts(limit);
    }

    @GetMapping("/top")
    @PreAuthorize("isAuthenticated()")
    public List<Post> getTopPosts(
            @RequestParam(defaultValue = "5") int limit
    ){
        return postService.getTopPosts(limit);
    }

    @GetMapping("/permanent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Post>> getPermanentPosts() {
        List<Post> posts = postService.getPermanentPosts();
        return ResponseEntity.ok(posts);
    }
}