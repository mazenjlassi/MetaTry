package com.example.metatry.Models;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private PlatformType platform;

    private Boolean generatedByAI;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private LocalDateTime scheduledAt;

    private LocalDateTime publishedAt;

    /*
     Latest metrics snapshot
    */
    private Integer likes;

    private Integer commentsCount;

    private Integer shares;

    private Integer impressions;

    private Double engagementScore;

    /*
     Platform ID after publishing
    */
    private String platformPostId;

    private Boolean approved = false;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    /*
     Metrics history
    */
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostMetric> metrics;

    /*
     Comments fetched from APIs
    */
    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostComment> comments;

    /*
 Image for this post
*/
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference
    private PostImage image;



}