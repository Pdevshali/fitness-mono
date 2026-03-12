package com.pdev.fitnessMono.dtos;

import com.pdev.fitnessMono.model.ActivityContext;
import com.pdev.fitnessMono.model.ActivityMetrics;
import com.pdev.fitnessMono.model.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private String id;
    private String userId;
    private ActivityType type;
    
    // Direct metrics and context instead of map
    private ActivityMetrics metrics;
    private ActivityContext context;

    private Integer duration; // Duration in minutes
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
