package com.example.metatry.DTOs;

import lombok.Data;

@Data
public class AiPostRequest {

    private String topic;

    private String platform;

    private Boolean generateImage;

    private Boolean generateVideo;
}