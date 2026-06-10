package com.superball.controller;

import com.superball.entity.User;
import com.superball.service.AdminService;
import com.superball.service.UserService;
import com.superball.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AdminService adminService;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader);
        return ResponseEntity.ok(userService.findById(userId));
    }

    @GetMapping("/points")
    public ResponseEntity<?> getTotalPoints(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader);
        return ResponseEntity.ok(Map.of("totalPoints", userService.getTotalPoints(userId)));
    }

    @GetMapping("/matches")
    public ResponseEntity<?> getAllMatches() {
        return ResponseEntity.ok(adminService.getAllMatches());
    }

    @GetMapping("/players")
    public ResponseEntity<?> getAllPlayers() {
        return ResponseEntity.ok(adminService.getAllPlayers());
    }

    @PutMapping("/nickname")
    public ResponseEntity<?> changeNickname(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Map<String, String> body) {
        Long userId = jwtUtil.extractUserId(authHeader);
        User updated = userService.changeNickname(userId, body.get("nickname"));
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/image")
    public ResponseEntity<?> changeImage(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody Map<String, String> body) {
        Long userId = jwtUtil.extractUserId(authHeader);
        User updated = userService.changeProfileImage(userId, body.get("imageUrl"));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader);
        userService.deleteAccount(userId);
        return ResponseEntity.ok("Account deleted successfully.");
    }
}
