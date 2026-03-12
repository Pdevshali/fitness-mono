package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.model.Activity;
import com.pdev.fitnessMono.model.ActivityType;
import com.pdev.fitnessMono.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityAnalysisService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    // Simplified analysis - returns only what AI really needs
    public Map<String, Object> getSimpleAnalysis(String userId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Activity> activities = activityRepository.findByUserId(userId).stream()
                .filter(a -> a.getCreatedAt().isAfter(cutoff))
                .collect(Collectors.toList());
        
        Map<String, Object> analysis = new HashMap<>();
        
        // Basic stats - simple and useful
        analysis.put("totalActivities", activities.size());
        analysis.put("mostFrequentType", getMostFrequentType(activities));
        analysis.put("averageDuration", calculateAverageDuration(activities));
        
        // Simple consistency - based on weekly frequency
        analysis.put("isConsistent", isConsistent(activities));
        
        return analysis;
    }
    
    private ActivityType getMostFrequentType(List<Activity> activities) {
        if (activities.isEmpty()) return ActivityType.OTHER;
        
        return activities.stream()
                .collect(Collectors.groupingBy(Activity::getType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActivityType.OTHER);
    }
    
    private double calculateAverageDuration(List<Activity> activities) {
        return activities.stream()
                .mapToInt(Activity::getDuration)
                .average()
                .orElse(0.0);
    }
    
    private boolean isConsistent(List<Activity> activities) {
        if (activities.size() < 4) return false;
        
        // Simple check: at least 3 activities per week on average
        double weeklyAverage = (double) activities.size() / 4.0;
        return weeklyAverage >= 3.0;
    }
}
