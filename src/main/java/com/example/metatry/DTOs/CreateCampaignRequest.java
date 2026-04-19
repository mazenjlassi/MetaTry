package com.example.metatry.DTOs;

import lombok.Data;

@Data
public class CreateCampaignRequest {

    private String name;
    private String topic;
    private int postNumber;
}