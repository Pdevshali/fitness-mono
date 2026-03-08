package com.pdev.fitnessMono.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdev.fitnessMono.dtos.GeminiResponse;
import com.pdev.fitnessMono.model.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final int MAX_RETRIES = 3;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    public GeminiResponse generateSuggestions(Activity activity) {

        // Groq uses OpenAI-compatible format
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", 0.4,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", buildPrompt(activity)
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

    private String buildPrompt(Activity activity) {
        return """
                You are a fitness expert AI. Analyze the following workout and respond ONLY with raw JSON.
                No markdown, no code fences, no extra text.

                Activity:
                - Type: %s
                - Duration: %d minutes
                - Calories Burned: %d
                - Additional Metrics: %s

                Respond in exactly this format:
                {"improvements": ["tip1", "tip2"], "suggestions": ["suggestion1", "suggestion2"], "safety": ["safety1", "safety2"]}
                """.formatted(
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMatrics()
        );
    }

    private GeminiResponse parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            // Groq/OpenAI response format: choices[0].message.content
            String text = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

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

            return new GeminiResponse(improvements, suggestions, safety);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq response: " + e.getMessage(), e);
        }
    }
}
