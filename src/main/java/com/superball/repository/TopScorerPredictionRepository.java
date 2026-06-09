package com.superball.repository;

import com.superball.entity.TopScorerPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopScorerPredictionRepository extends JpaRepository<TopScorerPrediction, Long> {

    Optional<TopScorerPrediction> findByUserId(Long userId);

    List<TopScorerPrediction> findByPlayerId(Long playerId);

    void deleteByUserId(Long userId);
}
