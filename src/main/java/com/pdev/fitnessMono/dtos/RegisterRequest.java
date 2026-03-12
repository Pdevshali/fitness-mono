package com.pdev.fitnessMono.dtos;

import com.pdev.fitnessMono.model.FitnessGoal;
import com.pdev.fitnessMono.model.FitnessLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
    private String firstName;
    private String lastName;

    // Optional fitness profile fields
    private Double weight; // in kg
    private Double height; // in cm
    private FitnessLevel fitnessLevel;
    private FitnessGoal primaryGoal;

}
