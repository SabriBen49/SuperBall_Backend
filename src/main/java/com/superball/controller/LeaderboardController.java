package com.superball.controller;

import com.superball.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<?> getLeaderboard() {
        return ResponseEntity.ok(adminService.getLeaderboard());
    }

    @GetMapping("/scorers")
    public ResponseEntity<?> getScorerLeaderboard() {
        return ResponseEntity.ok(adminService.getTopScorerLeaderboard());
    }
}
