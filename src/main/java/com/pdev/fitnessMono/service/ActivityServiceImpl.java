package com.pdev.fitnessMono.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pdev.fitnessMono.dtos.ActivityRequest;
import com.pdev.fitnessMono.dtos.ActivityResponse;
import com.pdev.fitnessMono.model.Activity;
import com.pdev.fitnessMono.model.User;
import com.pdev.fitnessMono.redis.RedisService;
import com.pdev.fitnessMono.repository.ActivityRepository;
import com.pdev.fitnessMono.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @Override
    public ActivityResponse trackActivity(ActivityRequest request) {
       User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        Activity activity = new Activity();
        activity.setType(request.getType());
        activity.setUser(user);
        activity.setDuration(request.getDuration());
        activity.setCaloriesBurned(request.getCaloriesBurned());
        activity.setStartTime(request.getStartTime());
        activity.setAdditionalMatrics(request.getAdditionalMatrics());

        Activity savedActivity = activityRepository.save(activity);
        return mapToResponse(savedActivity);
    }

    /**
     * Fetches activities with Redis cache-aside pattern.
     *
     * <p>Cache key: {@code user_activities:{userId}} | TTL: 600s</p>
     *
     * @param userId user UUID
     * @return cached or fresh activities
     */
    @Override
    public List<ActivityResponse> getTrackingActivities(String userId) {
        String cacheKey = "user_activities:" + userId;

        // Use TypeReference to preserve generic type information!
        List<ActivityResponse> cachedActivities = redisService.get(
                cacheKey,
                new TypeReference<List<ActivityResponse>>() {}
        );
        if (cachedActivities != null) {
            log.info("Cache hit for user: {}", userId);
            return cachedActivities;
        }

        log.info("Cache miss for user: {}, fetching from DB", userId);
        List<Activity> activities = activityRepository.findByUserId(userId);
        List<ActivityResponse> res = activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        redisService.set(cacheKey, res, 600);
        return res;
    }

    private ActivityResponse mapToResponse(Activity savedActivity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(savedActivity.getId());
        response.setUserId(savedActivity.getUser().getId());
        response.setType(savedActivity.getType());
        response.setDuration(savedActivity.getDuration());
        response.setCaloriesBurned(savedActivity.getCaloriesBurned());
        response.setStartTime(savedActivity.getStartTime());
        response.setAdditionalMatrics(savedActivity.getAdditionalMatrics());
        response.setCreatedAt(savedActivity.getCreatedAt());
        response.setUpdatedAt(savedActivity.getUpdatedAt());
        return response;
    }
}
