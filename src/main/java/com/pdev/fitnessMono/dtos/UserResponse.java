package com.pdev.fitnessMono.dtos;

import com.pdev.fitnessMono.model.FitnessGoal;
import com.pdev.fitnessMono.model.FitnessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    
    // Fitness profile fields
    private Double weight;
    private Double height;
    private FitnessLevel fitnessLevel;
    private FitnessGoal primaryGoal;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
