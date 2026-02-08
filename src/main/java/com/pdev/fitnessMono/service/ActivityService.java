package com.pdev.fitnessMono.service;

import com.pdev.fitnessMono.dtos.ActivityRequest;
import com.pdev.fitnessMono.dtos.ActivityResponse;
import com.pdev.fitnessMono.model.Activity;

import java.util.List;

public interface ActivityService {

    ActivityResponse trackActivity(ActivityRequest request);
    List<ActivityResponse> getTrackingActivities(String userId);

}
