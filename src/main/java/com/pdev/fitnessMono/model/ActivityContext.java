package com.pdev.fitnessMono.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityContext {
    // How it felt
    @Enumerated(EnumType.STRING)
    private IntensityLevel perceivedIntensity;
    
    private Integer rpe; // Rate of Perceived Exertion (1-10 scale)
    
    // Simple notes
    private String notes; // how they felt, any issues, etc.
}
