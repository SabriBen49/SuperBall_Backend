package com.superball.controller;

import com.superball.entity.TopScorerPrediction;
import com.superball.service.TopScorerService;
import com.superball.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/top-scorer")
@RequiredArgsConstructor
public class TopScorerController {

    private final TopScorerService topScorerService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> savePrediction(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.extractUserId(authHeader);
        Long playerId = Long.valueOf(body.get("playerId").toString());
        TopScorerPrediction prediction = topScorerService.savePrediction(userId, playerId);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPrediction(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader);
        return ResponseEntity.ok(topScorerService.getUserPrediction(userId));
    }
}
