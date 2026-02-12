package com.pdev.fitnessMono.controller;

import com.pdev.fitnessMono.dtos.ActivityRequest;
import com.pdev.fitnessMono.dtos.ActivityResponse;
import com.pdev.fitnessMono.model.Activity;
import com.pdev.fitnessMono.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@PathVariable String userId) {

        return ResponseEntity.ok(activityService.getTrackingActivities(userId));
    }
}
