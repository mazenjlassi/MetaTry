package com.example.metatry.Services;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildPrompt(String topic){

        return """
You are a professional social media marketing expert.

Generate content for a social media post.

Topic:
""" + topic + """

Return ONLY valid JSON with this structure:

{
 "postText": "",
 "hashtags": ["", ""],
 "imagePrompt": "",
 "videoScript": "",
 "videoPrompt": "",
 "cta": ""
}
""";
    }
}