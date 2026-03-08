package com.pdev.fitnessMono.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class GeminiResponse {

    private List<String> improvements;
    private List<String> suggestions;
    private List<String> safety;

}
