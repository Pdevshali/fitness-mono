package com.pdev.fitnessMono.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMetrics {
    // Distance-based activities (running, cycling, swimming)
    private Double distance; // in km
    
    // Cardio metrics
    private Integer averageHeartRate; // bpm
    
    // Strength training
    private Double weightLifted; // kg
    private Integer sets;
    private Integer reps;
    
    // General
    private Integer steps; // for walking/running
    private String notes; // free text for anything else
}
