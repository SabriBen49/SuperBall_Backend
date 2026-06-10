package com.superball.service;

import com.superball.entity.Prediction;

import java.util.List;

public interface PredictionService {

    
    Prediction createPrediction(Long userId, Long matchId, int homeScore, int awayScore, Integer totalCards);

    
    Prediction updatePrediction(Long predictionId, Long userId, int homeScore, int awayScore, Integer totalCards);

    
    List<Prediction> getUserPredictions(Long userId);

    
    void calculatePointsForMatch(Long matchId);
}

