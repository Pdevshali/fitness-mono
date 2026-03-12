package com.pdev.fitnessMono.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdev.fitnessMono.dtos.AiResponse;
import com.pdev.fitnessMono.model.*;
import com.pdev.fitnessMono.repository.ActivityRepository;
import com.pdev.fitnessMono.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final int MAX_RETRIES = 3;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private ActivityAnalysisService analysisService;

    @Autowired
    private UserRepository userRepository;

    public AiResponse generateSuggestions(String userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        List<Activity> recentActivities = activityRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        Map<String, Object> simpleAnalysis = analysisService.getSimpleAnalysis(userId);
        
        // Groq uses OpenAI-compatible format
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", 0.4,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", buildEnhancedPrompt(user, recentActivities, simpleAnalysis)
                        )
                )
        );

        String response = callWithRetry(requestBody);
        log.debug("Groq raw response: {}", response);
        return parseResponse(response);
    }

    private String callWithRetry(Map<String, Object> requestBody) {
        int attempt = 0;
        while (true) {
            try {
                return restClient.post()
                        .uri(apiUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + apiKey)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("Groq API rate limit exceeded after " + MAX_RETRIES + " retries", e);
                }
                log.warn("Rate limit hit. Retrying in {}s (attempt {}/{})", attempt * 2, attempt, MAX_RETRIES);
                try {
                    Thread.sleep(attempt * 2000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }

    private String buildEnhancedPrompt(User user, List<Activity> recentActivities, Map<String, Object> simpleAnalysis) {
        StringBuilder recentActivitiesStr = new StringBuilder();
        if (!recentActivities.isEmpty()) {
            recentActivitiesStr.append("Recent Activities:\n");
            for (int i = 0; i < recentActivities.size(); i++) {
                Activity recent = recentActivities.get(i);
                recentActivitiesStr.append(String.format("- %s (%d min)\n",
                        recent.getType(), recent.getDuration()));
            }
        }

        // Build user profile with available data only
        StringBuilder userProfile = new StringBuilder();
        userProfile.append(String.format("- Fitness Level: %s\n", user.getFitnessLevel()));
        userProfile.append(String.format("- Goal: %s\n", user.getPrimaryGoal()));
        
        if (user.getWeight() != null && user.getHeight() != null) {
            userProfile.append(String.format("- Weight: %.1f kg, Height: %.1f cm\n", user.getWeight(), user.getHeight()));
        }

        // Add simple analysis insights
        StringBuilder insights = new StringBuilder();
        if (simpleAnalysis != null) {
            Integer totalActivities = (Integer) simpleAnalysis.get("totalActivities");
            ActivityType mostFrequent = (ActivityType) simpleAnalysis.get("mostFrequentType");
            Boolean isConsistent = (Boolean) simpleAnalysis.get("isConsistent");
            Double avgDuration = (Double) simpleAnalysis.get("averageDuration");
            
            if (totalActivities != null && totalActivities > 0) {
                insights.append(String.format("- Total activities (30 days): %d\n", totalActivities));
            }
            if (mostFrequent != null && mostFrequent != ActivityType.OTHER) {
                insights.append(String.format("- Favorite activity: %s\n", mostFrequent));
            }
            if (isConsistent != null) {
                insights.append(String.format("- Consistency: %s\n", isConsistent ? "Good (3+ per week)" : "Could be more consistent"));
            }
            if (avgDuration != null && avgDuration > 0) {
                insights.append(String.format("- Average duration: %.0f minutes\n", avgDuration));
            }
        }

        return """
                You are a fitness AI coach. Provide personalized recommendations based on this user's fitness journey:
                
                USER PROFILE:
                %s
                
                %s
                
                USER INSIGHTS:
                %s
                
                Provide simple, actionable advice. Consider their fitness level, goals, and activity patterns.
                Focus on overall fitness improvement, consistency, and variety.
                
                Respond ONLY with raw JSON:
                {"improvements": ["tip1", "tip2"], "suggestions": ["suggestion1", "suggestion2"], "safety": ["safety1", "safety2"]}
                """.formatted(
                userProfile.toString(),
                recentActivitiesStr.length() > 0 ? "RECENT ACTIVITIES:\n" + recentActivitiesStr.toString() : "",
                insights.toString()
        );
    }

    private AiResponse parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            // Groq/OpenAI response format: choices[0].message.content
            JsonNode choices = root.path("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("API response missing choices array");
            }
            
            JsonNode firstChoice = choices.get(0);
            if (firstChoice == null) {
                throw new RuntimeException("API response choices array is empty");
            }
            
            JsonNode message = firstChoice.path("message");
            if (message == null || message.isMissingNode()) {
                throw new RuntimeException("API response missing message in choice");
            }
            
            JsonNode content = message.path("content");
            if (content == null || content.isMissingNode()) {
                throw new RuntimeException("API response missing content in message");
            }
            
            String text = content.asText();

            log.debug("AI response content: {}", text);

            // Strip markdown fences if model still adds them
            String cleaned = text.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned
                        .replaceAll("^```[a-zA-Z]*\\n?", "")
                        .replaceAll("```$", "")
                        .trim();
            }

            JsonNode json = objectMapper.readTree(cleaned);
            List<String> improvements = objectMapper.convertValue(json.get("improvements"), List.class);
            List<String> suggestions  = objectMapper.convertValue(json.get("suggestions"),  List.class);
            List<String> safety       = objectMapper.convertValue(json.get("safety"),        List.class);

            return new AiResponse(improvements, suggestions, safety);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq response: " + e.getMessage(), e);
        }
    }
}
