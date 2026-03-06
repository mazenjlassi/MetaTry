package com.example.metatry.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class AiGeneratedContent {

    private String postText;

    private List<String> hashtags;

    private String imagePrompt;

    private String videoScript;

    private String videoPrompt;

    private String cta;
}