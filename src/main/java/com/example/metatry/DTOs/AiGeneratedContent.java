package com.example.metatry.DTOs;

import lombok.Data;
import java.util.List;
@Data
public class AiGeneratedContent {

    private String linkedinPost;
    private List<String> linkedinHashtags;

    private String instagramPost;
    private List<String> instagramHashtags;

    private String facebookPost;
    private List<String> facebookHashtags;

}