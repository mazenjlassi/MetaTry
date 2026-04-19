package com.example.metatry.Services;

import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Repositories.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final AiContentService aiContentService;

    // 🔥 Create campaign + generate posts
    public List<Post> createCampaignAndGeneratePosts(CreateCampaignRequest request) {

        Campaign campaign = Campaign.builder()
                .name(request.getName())           // ✅ NEW
                .topic(request.getTopic())
                .createdAt(LocalDateTime.now())
                .build();

        campaignRepository.save(campaign);

        return aiContentService.generatePostsWithCampaign(
                request.getTopic(),
                request.getPostNumber(),
                campaign
        );
    }

    // 📊 Get all campaigns
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAllByOrderByCreatedAtDesc();
    }

    // 📊 Get one campaign
    public Campaign getCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
    }

    // ❌ Delete campaign
    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
    }
}