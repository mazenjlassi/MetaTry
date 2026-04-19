package com.example.metatry.Services;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildPrompt(String topic){

        return """
You are an expert social media marketing strategist.

Create three DIFFERENT high-performing marketing posts for:

1. LinkedIn → professional, storytelling, authority
2. Instagram → short, catchy, emoji-driven
3. Facebook → conversational, engaging, community-driven

Topic:
""" + topic + """

Each platform must have:
- a compelling TITLE
- optimized content
- relevant hashtags

Rules:
- LinkedIn: max 1200 characters
- Instagram: max 220 characters
- Facebook: max 500 characters

- Titles must be short, engaging, and platform-adapted
- Do NOT repeat the same content across platforms
- Make posts feel natural and human-written
- Include call-to-action when relevant

Return ONLY valid JSON.

JSON format:

{
 "linkedinTitle": "...",
 "linkedinPost": "...",
 "linkedinHashtags": ["AI","Automation"],

 "instagramTitle": "...",
 "instagramPost": "...",
 "instagramHashtags": ["AI","Startup"],

 "facebookTitle": "...",
 "facebookPost": "...",
 "facebookHashtags": ["AI","Business"],

 "imagePrompt": "A professional marketing visual related to the topic"
}
""";
    }
}