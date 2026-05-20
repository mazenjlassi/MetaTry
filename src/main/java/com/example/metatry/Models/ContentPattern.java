package com.example.metatry.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_patterns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic")
    private String topic;

    @Column(name = "platform")
    private String platform;

    @Column(name = "post_frequency", length = 100)
    private String postFrequency;

    @Column(name = "content_length", length = 100)
    private String contentLength;

    @Column(name = "media_type", length = 100)
    private String mediaType;

    @Column(name = "hashtag_count", length = 100)
    private String hashtagCount;

    @Column(name = "timing_pattern", length = 200)
    private String timingPattern;

    @Column(name = "tone", length = 255)
    private String tone;

    @Column(name = "cta_style", length = 500)
    private String ctaStyle;

    @Column(name = "total_posts_analyzed")
    private Integer totalPostsAnalyzed;

    @Column(name = "ai_analysis_raw", columnDefinition = "TEXT")
    private String aiAnalysisRaw;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    protected void onCreate() {
        extractedAt = LocalDateTime.now();
        lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}