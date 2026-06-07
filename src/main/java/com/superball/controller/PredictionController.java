package com.superball.controller;

import com.superball.entity.Prediction;
import com.superball.service.PredictionService;
import com.superball.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createPrediction(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.extractUserId(authHeader);
        Long matchId = Long.valueOf(body.get("matchId").toString());
        int homeScore = Integer.parseInt(body.get("homeScore").toString());
        int awayScore = Integer.parseInt(body.get("awayScore").toString());
        Integer totalCards = body.get("totalCards") != null
                ? Integer.parseInt(body.get("totalCards").toString())
                : null;

        Prediction prediction = predictionService.createPrediction(userId, matchId, homeScore, awayScore, totalCards);
        return ResponseEntity.ok(prediction);
    }

    @PutMapping("/{predictionId}")
    public ResponseEntity<?> updatePrediction(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable Long predictionId,
                                              @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.extractUserId(authHeader);
        int homeScore = Integer.parseInt(body.get("homeScore").toString());
        int awayScore = Integer.parseInt(body.get("awayScore").toString());
        Integer totalCards = body.get("totalCards") != null
                ? Integer.parseInt(body.get("totalCards").toString())
                : null;

        Prediction updated = predictionService.updatePrediction(predictionId, userId, homeScore, awayScore, totalCards);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPredictions(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader);
        List<Prediction> predictions = predictionService.getUserPredictions(userId);
        return ResponseEntity.ok(predictions);
    }
}
