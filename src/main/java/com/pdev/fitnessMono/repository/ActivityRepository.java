package com.pdev.fitnessMono.repository;

import com.pdev.fitnessMono.model.Activity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends CrudRepository<Activity, String> {
    List<Activity> findByUserId(String userId);
    List<Activity> findTop5ByUserIdOrderByCreatedAtDesc(String userId);

    Activity findTop1ByUserIdOrderByCreatedAtDesc(String userId);
}
