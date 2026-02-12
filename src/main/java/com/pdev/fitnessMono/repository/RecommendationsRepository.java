package com.pdev.fitnessMono.repository;

import com.pdev.fitnessMono.model.Recommendations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationsRepository extends JpaRepository<Recommendations, String> {
    List<Recommendations> findByUserId(String userId);

    List<Recommendations> findByActivityId(String id);
}
