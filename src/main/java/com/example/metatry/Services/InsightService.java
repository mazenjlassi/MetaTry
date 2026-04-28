package com.example.metatry.Services;

import com.example.metatry.DTO.PostInsightDTO;
import com.example.metatry.Models.PostComment;
import com.example.metatry.Repositories.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final PostCommentRepository commentRepository;

    public PostInsightDTO generatePostInsights(Long postId){

        List<PostComment> comments = commentRepository.findByPostId(postId);

        if(comments.isEmpty()){
            return emptyInsight();
        }

        return buildInsightFromComments(comments);
    }

    private String getOverallSentiment(double pos, double neg, double neu){
        if(pos > neg && pos > neu) return "POSITIVE";
        if(neg > pos && neg > neu) return "NEGATIVE";
        return "NEUTRAL";
    }

    // 🔥 Improved keyword extraction
    private List<String> extractKeywords(List<String> texts){

        Map<String, Integer> freq = new HashMap<>();

        List<String> stopWords = List.of(
                "good","great","nice","cool","love","very","this","that","with","have"
        );

        for(String text : texts){
            String[] words = text.toLowerCase().split("\\W+");

            for(String word : words){
                if(word.length() < 4 || stopWords.contains(word)) continue;

                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }

        return freq.entrySet().stream()
                .sorted((a,b) -> b.getValue() - a.getValue())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String buildSummary(String overall, List<String> positives, List<String> negatives){

        return "Overall sentiment is " + overall +
                ". Users like: " + positives +
                ". Complaints: " + negatives;
    }

    // 🔥 NEW: Advice logic
    private String generateAdvice(String overall, List<String> positives, List<String> negatives){

        if("NEGATIVE".equals(overall)){
            return "Content needs improvement. Focus on clarity, shorter posts, and a more engaging tone.";
        }

        if("NEUTRAL".equals(overall)){
            return "Content is average. Add stronger hooks, questions, or emotional triggers to increase engagement.";
        }

        if(!negatives.isEmpty()){
            return "Content performs well but can be improved. Address these issues: " + negatives;
        }

        return "Content performs very well. Keep the same style and engagement strategy.";
    }

    // 🔥 NEW: Ideas generator
    private List<String> generateIdeas(String overall, List<String> positives, List<String> negatives){

        List<String> ideas = new ArrayList<>();

        if("NEGATIVE".equals(overall)){
            ideas.add("Use shorter and clearer sentences");
            ideas.add("Add a strong hook at the beginning");
            ideas.add("Make posts more conversational");
        }

        if("NEUTRAL".equals(overall)){
            ideas.add("Add a question at the end of posts");
            ideas.add("Use emojis to increase engagement");
            ideas.add("Try storytelling format");
        }

        if("POSITIVE".equals(overall)){
            ideas.add("Reuse this content style");
            ideas.add("Create similar posts with variations");
            ideas.add("Expand this topic into a campaign");
        }

        return ideas;
    }

    private PostInsightDTO emptyInsight(){
        return PostInsightDTO.builder()
                .overallSentiment("NEUTRAL")
                .positiveRatio(0)
                .negativeRatio(0)
                .neutralRatio(1)
                .topComplaints(List.of())
                .topPositives(List.of())
                .summary("No comments available")
                .advice("No data available yet")
                .ideas(List.of("Start engaging your audience to collect feedback"))
                .build();
    }


    public PostInsightDTO generateCampaignInsights(Long campaignId){

        List<PostComment> comments = commentRepository.findByPostCampaignId(campaignId);

        if(comments.isEmpty()){
            return emptyInsight();
        }

        return buildInsightFromComments(comments);
    }
    private PostInsightDTO buildInsightFromComments(List<PostComment> comments){

        int total = comments.size();

        long positive = comments.stream().filter(c -> "POSITIVE".equals(c.getSentiment())).count();
        long negative = comments.stream().filter(c -> "NEGATIVE".equals(c.getSentiment())).count();
        long neutral  = comments.stream().filter(c -> "NEUTRAL".equals(c.getSentiment())).count();

        double posRatio = (double) positive / total;
        double negRatio = (double) negative / total;
        double neuRatio = (double) neutral  / total;

        String overall = getOverallSentiment(posRatio, negRatio, neuRatio);

        List<String> positives = extractKeywords(
                comments.stream()
                        .filter(c -> "POSITIVE".equals(c.getSentiment()))
                        .map(PostComment::getCommentText)
                        .toList()
        );

        List<String> negatives = extractKeywords(
                comments.stream()
                        .filter(c -> "NEGATIVE".equals(c.getSentiment()))
                        .map(PostComment::getCommentText)
                        .toList()
        );

        String summary = buildSummary(overall, positives, negatives);
        String advice = generateAdvice(overall, positives, negatives);
        List<String> ideas = generateIdeas(overall, positives, negatives);

        return PostInsightDTO.builder()
                .overallSentiment(overall)
                .positiveRatio(posRatio)
                .negativeRatio(negRatio)
                .neutralRatio(neuRatio)
                .topPositives(positives)
                .topComplaints(negatives)
                .summary(summary)
                .advice(advice)
                .ideas(ideas)
                .build();
    }
}