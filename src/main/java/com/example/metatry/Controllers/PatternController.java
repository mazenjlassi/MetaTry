package com.example.metatry.Controllers;

import com.example.metatry.DTOs.PatternAnalysisRequest;
import com.example.metatry.DTOs.PatternResponse;
import com.example.metatry.Models.ContentPattern;
import com.example.metatry.Services.PatternAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternAnalysisService patternAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<PatternResponse> analyzePattern(@RequestBody PatternAnalysisRequest request) {
        if (request.getTopic() == null || request.getTopic().isBlank()) {
            return ResponseEntity.badRequest().body(
                PatternResponse.builder()
                    .status("error")
                    .message("topic is required")
                    .build()
            );
        }
        PatternResponse response = patternAnalysisService.analyzePattern(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ContentPattern>> getAllPatterns() {
        return ResponseEntity.ok(patternAnalysisService.getAllPatterns());
    }

    @GetMapping("/{topic}")
    public ResponseEntity<ContentPattern> getPatternByTopic(@PathVariable String topic) {
        ContentPattern pattern = patternAnalysisService.getPatternByTopic(topic);
        if (pattern == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pattern);
    }

    @GetMapping("/match")
    public ResponseEntity<ContentPattern> matchPattern(@RequestParam String topic) {
        ContentPattern pattern = patternAnalysisService.getPatternByTopic(topic);
        if (pattern == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pattern);
    }
}