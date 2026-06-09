package com.superball.repository;

import com.superball.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    List<Prediction> findByMatchId(Long matchId);

    Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);

    List<Prediction> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
