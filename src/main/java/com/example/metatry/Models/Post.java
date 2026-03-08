package com.example.metatry.Models;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5000)
    private String content;

    @Column(length = 1000)
    private String hashtags;

    private String imageUrl;

    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private PlatformType platform; // FACEBOOK / INSTAGRAM / LINKEDIN

    private Boolean generatedByAI;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private LocalDateTime scheduledAt;

    private LocalDateTime publishedAt;

    private Integer likes;

    private Integer commentsCount;

    private Integer shares;

    private Integer impressions;

    private Double engagementScore;

    private Boolean approved = false;

    @ManyToOne
    private Campaign campaign;
}
