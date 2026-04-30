package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.PostSummaryDTO;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final PostService postService;

    // 🔥 KEEP THIS AS IS (your constraint)
    @PostMapping("/generate")
    public List<Post> generateCampaign(@RequestBody CreateCampaignRequest request) {
        return campaignService.createCampaignAndGeneratePosts(request);
    }

    // 📊 Get all campaigns
    @GetMapping
    public List<CampaignDTO> getAllCampaigns() {
        return campaignService.getAllCampaigns();
    }

    // 📊 Get one campaign
    @GetMapping("/{id}")
    public ResponseEntity<CampaignDTO> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignDTO(id));
    }

    // 📊 Get posts of a campaign (use SUMMARY, not full entity)
    @GetMapping("/{campaignId}/posts")
    public List<PostSummaryDTO> getPostsByCampaign(@PathVariable Long campaignId) {
        return postService.getPostSummariesByCampaign(campaignId);
    }

    // ❌ Delete campaign
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}