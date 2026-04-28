package com.example.metatry.Controllers;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Services.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping("/post/{postId}")
    public PostInsightDTO getPostInsights(@PathVariable Long postId){
        return insightService.generatePostInsights(postId);
    }

    @GetMapping("/campaign/{campaignId}")
    public PostInsightDTO getCampaignInsights(@PathVariable Long campaignId){
        return insightService.generateCampaignInsights(campaignId);
    }
}