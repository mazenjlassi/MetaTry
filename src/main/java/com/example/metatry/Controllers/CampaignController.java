package com.example.metatry.Controllers;

import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Services.CampaignService;
import com.example.metatry.Services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final PostService postService;

    // 🔥 Create campaign + generate posts
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
    public Campaign getCampaign(@PathVariable Long id) {
        return campaignService.getCampaign(id);
    }

    // ❌ Delete
    @DeleteMapping("/{id}")
    public String deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return "Campaign deleted";
    }
    @GetMapping("/{campaignId}/posts")
    public List<Post> getPostsByCampaign(@PathVariable Long campaignId) {
        return postService.getPostsByCampaign(campaignId);
    }

}