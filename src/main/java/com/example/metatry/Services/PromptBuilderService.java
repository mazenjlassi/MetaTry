package com.example.metatry.Services;

import org.springframework.stereotype.Service;
@Service
public class PromptBuilderService {

    public String buildPrompt(String topic){

        return """
You are an expert social media marketing strategist.

Create three different marketing posts optimized for each platform:

1. LinkedIn → professional tone, storytelling, thought leadership
2. Instagram → short caption, emojis, engaging, visually focused
3. Facebook → conversational, community focused, encourages interaction

Topic:
""" + topic + """

Each post must be optimized for reach and engagement.

Rules:
- LinkedIn: max 1200 characters
- Instagram: max 220 characters
- Facebook: max 500 characters
- Use appropriate hashtags for each platform
- Avoid repeating the same text across platforms

Return ONLY valid JSON.

JSON format:

{
 "linkedinPost": "...",
 "linkedinHashtags": ["AI","Automation"],
 "instagramPost": "...",
 "instagramHashtags": ["AI","Startup"],
 "facebookPost": "...",
 "facebookHashtags": ["AI","Business"],
 "imagePrompt": ""
""";
    }
}