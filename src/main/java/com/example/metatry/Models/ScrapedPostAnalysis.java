package com.example.metatry.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraped_post_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapedPostAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scraped_post_id")
    private Long scrapedPostId;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }
}