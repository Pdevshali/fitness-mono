package com.pdev.fitnessMono.controller;

import com.pdev.fitnessMono.dtos.RecommendationsRequest;
import com.pdev.fitnessMono.model.Recommendations;
import com.pdev.fitnessMono.service.RecommendationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    @Autowired
    private RecommendationsService recommendationService;

    @PostMapping("/generate")
    public ResponseEntity<Recommendations> generateRecommendations(@RequestBody RecommendationsRequest request) {
        Recommendations recommendations = recommendationService.generateRecommendations(request);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<Recommendations>> getRecommendationsForUser(@PathVariable String userId) {
        List<Recommendations> recommendations = recommendationService.getRecommendationsForUser(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("activity/{activityId}")
    public ResponseEntity<List<Recommendations>> getRecommendationsForActivity(@PathVariable String activityId) {
        List<Recommendations> recommendations = recommendationService.getRecommendationsForActivity(activityId);
        return ResponseEntity.ok(recommendations);
    }

}
