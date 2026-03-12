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
public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    
    // Optional advanced fields
    private ActivityMetrics metrics;
    private ActivityContext context;
}
