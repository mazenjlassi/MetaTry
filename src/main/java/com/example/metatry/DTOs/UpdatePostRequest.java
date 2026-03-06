package com.example.metatry.DTOs;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdatePostRequest {

    private String content;

    private String hashtags;

    private String platform;

    private String imageUrl;

    private String videoUrl;

    private Boolean approved;

    private LocalDateTime scheduledAt;

}