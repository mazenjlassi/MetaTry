package com.example.metatry.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CampaignDTO {

    private Long id;
    private String name;
    private String topic;
    private int postCount;
}