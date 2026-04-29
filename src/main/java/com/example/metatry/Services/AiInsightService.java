package com.example.metatry.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInsightService {

    private final GeminiService geminiService; // you already use Gemini
    private final ObjectMapper objectMapper;

    public String analyzeComments(List<String> comments){

        String joinedComments = String.join("\n- ", comments);

        String prompt = """
You are a marketing analyst.

Analyze the following user comments and extract insights.

Return ONLY valid JSON.

STRICT RULES:
- Do NOT return text outside JSON
- Do NOT explain anything
- "topPositives" MUST be an array of short keywords (max 3 words each)
- "topComplaints" MUST be an array
- NEVER return sentences for these fields
- If no data, return empty array []

Correct example:

{
  "overallSentiment": "POSITIVE",
  "topPositives": ["easy use", "fast", "clean UI"],
  "topComplaints": [],
  "summary": "Users are satisfied",
  "advice": "Keep same approach",
  "ideas": ["Add features", "Improve UX"]
}

Comments:
- """ + joinedComments;

        return geminiService.generate(prompt);
    }
}