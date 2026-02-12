package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.dtos.RecommendationsRequest;
import com.pdev.fitnessMono.model.Recommendations;
import java.util.List;

public interface RecommendationsService {
    Recommendations generateRecommendations(RecommendationsRequest request);

    List<Recommendations> getRecommendationsForUser(String userId);

    List<Recommendations> getRecommendationsForActivity(String activityId);
}
