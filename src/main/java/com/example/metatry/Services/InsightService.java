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

        return PostInsightDTO.builder()
                .overallSentiment(overall)
                .positiveRatio(posRatio)
                .negativeRatio(negRatio)
                .neutralRatio(neuRatio)
                .topPositives(positives)
                .topComplaints(negatives)
                .summary(summary)
                .build();
    }

    private String getOverallSentiment(double pos, double neg, double neu){
        if(pos > neg && pos > neu) return "POSITIVE";
        if(neg > pos && neg > neu) return "NEGATIVE";
        return "NEUTRAL";
    }

    private List<String> extractKeywords(List<String> texts){

        Map<String, Integer> freq = new HashMap<>();

        for(String text : texts){
            String[] words = text.toLowerCase().split("\\W+");

            for(String word : words){
                if(word.length() < 4) continue;

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

    private PostInsightDTO emptyInsight(){
        return PostInsightDTO.builder()
                .overallSentiment("NEUTRAL")
                .positiveRatio(0)
                .negativeRatio(0)
                .neutralRatio(1)
                .topComplaints(List.of())
                .topPositives(List.of())
                .summary("No comments available")
                .build();
    }
}