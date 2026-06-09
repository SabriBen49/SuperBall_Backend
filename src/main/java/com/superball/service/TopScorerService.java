package com.superball.service;

import com.superball.entity.TopScorerPrediction;

public interface TopScorerService {

    
    TopScorerPrediction savePrediction(Long userId, Long playerId);

    
    TopScorerPrediction getUserPrediction(Long userId);

    
    void calculateTopScorerPoints();
}

