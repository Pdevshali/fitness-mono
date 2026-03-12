package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.dtos.AiResponse;
import com.pdev.fitnessMono.dtos.RecommendationsRequest;
import com.pdev.fitnessMono.model.Activity;
import com.pdev.fitnessMono.model.Recommendations;
import com.pdev.fitnessMono.model.User;
import com.pdev.fitnessMono.repository.ActivityRepository;
import com.pdev.fitnessMono.repository.RecommendationsRepository;
import com.pdev.fitnessMono.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationsServiceImpl implements RecommendationsService {

    @Autowired
    private RecommendationsRepository recommendationsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private AiService aiService;

    @Override
    public Recommendations generateRecommendations(RecommendationsRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user's most recent activity for context
        Activity mostRecentActivity = activityRepository.findTop1ByUserIdOrderByCreatedAtDesc(request.getUserId());

        if (mostRecentActivity == null) {
            throw new RuntimeException("No activities found for user. User needs to log some activities first.");
        }
        
        // Generate user-specific suggestions based on recent activities
        AiResponse aiResponse = aiService.generateSuggestions(request.getUserId());

        Recommendations recommendations = new Recommendations();
        recommendations.setUser(user);
        recommendations.setActivity(mostRecentActivity);
        recommendations.setImprovements(aiResponse.getImprovements());
        recommendations.setSuggestions(aiResponse.getSuggestions());
        recommendations.setSafety(aiResponse.getSafety());

        return recommendationsRepository.save(recommendations);
    }

    @Override
    public List<Recommendations> getRecommendationsForUser(String userId) {
        return recommendationsRepository.findByUserId(userId);
    }

    @Override
    public List<Recommendations> getRecommendationsForActivity(String activityId) {
        return recommendationsRepository.findByActivityId(activityId);
    }
}
