package com.example.metatry.Services;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildPrompt(String topic, String insights){

        return """
You are an expert social media marketing strategist.

Create three DIFFERENT high-performing marketing posts for:

1. LinkedIn → professional, storytelling, authority
2. Instagram → short, catchy, emoji-driven
3. Facebook → conversational, engaging, community-driven

========================
TOPIC:
""" + topic + """

========================
AUDIENCE INSIGHTS:
""" + insights + """

========================
INSTRUCTIONS:

- Adapt content based on audience feedback
- Reinforce what users liked
- Fix what users complained about
- Improve engagement, clarity, and tone
- If feedback is positive → replicate style
- If feedback is negative → correct issues

========================
CONTENT REQUIREMENTS:

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

========================
OUTPUT FORMAT:

Return ONLY valid JSON.

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