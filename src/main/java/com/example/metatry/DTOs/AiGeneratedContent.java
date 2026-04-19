package com.example.metatry.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class AiGeneratedContent {

    //  TITLES
    private String linkedinTitle;
    private String instagramTitle;
    private String facebookTitle;

    //  CONTENT
    private String linkedinPost;
    private String instagramPost;
    private String facebookPost;

    //  HASHTAGS
    private List<String> linkedinHashtags;
    private List<String> instagramHashtags;
    private List<String> facebookHashtags;

    //  IMAGE
    private String imagePrompt;
}