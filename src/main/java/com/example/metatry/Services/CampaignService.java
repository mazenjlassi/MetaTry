package com.example.metatry.Services;

import com.example.metatry.DTOs.CreateCampaignRequest;
import com.example.metatry.DTOs.CampaignDTO;
import com.example.metatry.DTOs.CreatePostRequest;
import com.example.metatry.DTOs.CampaignProgressDTO;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Models.Campaign;
import com.example.metatry.Models.Post;
import com.example.metatry.Models.PostImage;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.PostRepository;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final PostRepository postRepository;
    private final CampaignRepository campaignRepository;
    private final AiContentService aiContentService;

    private final CloudinaryService cloudinaryService;
    private final InsightService insightService;
    private final ChatService chatService;

    public List<Post> createCampaignAndGeneratePosts(CreateCampaignRequest request) {

        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .topic(request.getTopic())
                .createdAt(LocalDateTime.now())
                .build();

        campaignRepository.save(campaign);

        String insights = "No insights yet";
        try {
            insights = insightService.generateCampaignInsights(campaign.getId()).getSummary();
        } catch (Exception ignored) {}

        String conclusion = "Focus on engagement and clarity";
        try {
            if (request.getConversationId() != null) {
                conclusion = chatService.generateConclusion(request.getConversationId());
            }
        } catch (Exception ignored) {}

        return aiContentService.generatePostsWithCampaign(
                request.getTopic(),
                campaign,
                insights,
                conclusion
        );
    }

    public List<Post> generatePostsForExistingCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        String insights = "No insights yet";
        try {
            insights = insightService.generateCampaignInsights(campaign.getId()).getSummary();
        } catch (Exception ignored) {}

        String conclusion = "Focus on engagement and clarity";
        try {
            conclusion = chatService.generateConclusion(null);
        } catch (Exception ignored) {}

        return aiContentService.generatePostsWithCampaign(
                campaign.getTopic(),
                campaign,
                insights,
                conclusion
        );
    }

    public Post createPostForCampaign(
            Long campaignId,
            CreatePostRequest request,
            List<MultipartFile> images
    ) throws IOException, java.io.IOException {

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        Post post = new Post();

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setHashtags(request.getHashtags());
        post.setPlatform(request.getPlatform());

        post.setGeneratedByAI(false);
        post.setApproved(true);
        post.setStatus(PostStatus.SCHEDULED);
        post.setScheduledAt(request.getScheduledAt());
        post.setPermanent(request.isPermanent());

        String link = request.getLink();
        if (link == null || link.isBlank()) {
            link = "https://3lm-solutions2.odoo.com/contactus";
        }
        post.setLink(link);

        post.setCampaign(campaign);

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file != null && !file.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(file);
                    PostImage postImage = PostImage.builder()
                            .imageUrl(imageUrl)
                            .sortOrder(i)
                            .post(post)
                            .selected(true)
                            .build();
                    postImages.add(postImage);
                }
            }
            post.setImages(postImages);
        }

        return postRepository.save(post);
    }

    public List<CampaignDTO> getAllCampaigns() {
        return campaignRepository.findAllWithPosts().stream()
                .map(c -> CampaignDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .topic(c.getTopic())
                        .postCount(c.getPosts() != null ? c.getPosts().size() : 0)
                        .build())
                .toList();
    }

    public CampaignDTO getCampaignDTO(Long id) {

        Campaign c = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return CampaignDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .topic(c.getTopic())
                .postCount(c.getPosts() != null ? c.getPosts().size() : 0)
                .build();
    }

    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
    }

    public Campaign getCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
    }

    public List<Campaign> getAllCampaignsRaw() {
        return campaignRepository.findAll();
    }

    public Campaign createManualCampaign(CreateCampaignRequest request) {

        Campaign campaign = new Campaign();

        campaign.setName(request.getName());
        campaign.setTopic(request.getTopic());

        return campaignRepository.save(campaign);
    }

    public List<CampaignProgressDTO> getCampaignsWithProgress(int limit) {
        List<Campaign> campaigns = campaignRepository.findAll();

        return campaigns.stream()
            .limit(limit)
            .map(c -> {
                List<Post> posts = postRepository.findByCampaignId(c.getId());
                int totalPosts = posts.size();
                int publishedPosts = (int) posts.stream()
                    .filter(p -> p.getStatus() == PostStatus.PUBLISHED)
                    .count();

                return CampaignProgressDTO.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .topic(c.getTopic())
                    .totalPosts(totalPosts)
                    .publishedPosts(publishedPosts)
                    .status(c.getCreatedAt() != null ? "Active" : "Draft")
                    .build();
            })
            .toList();
    }

    public List<CampaignDTO> getRecentCampaigns(int limit) {
        return campaignRepository.findAll().stream()
            .sorted((c1, c2) -> {
                if (c1.getCreatedAt() == null) return 1;
                if (c2.getCreatedAt() == null) return -1;
                return c2.getCreatedAt().compareTo(c1.getCreatedAt());
            })
            .limit(limit)
            .map(c -> CampaignDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .topic(c.getTopic())
                .postCount(c.getPosts() != null ? c.getPosts().size() : 0)
                .build())
            .toList();
    }
}