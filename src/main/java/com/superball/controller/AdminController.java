package com.superball.controller;

import com.superball.entity.Match;
import com.superball.entity.Player;
import com.superball.entity.Quiz;
import com.superball.entity.Team;
import com.superball.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/teams")
    public ResponseEntity<?> createTeam(@RequestBody Map<String, String> body) {
        Team team = adminService.createTeam(body.get("name"), body.get("flagImageUrl"));
        return ResponseEntity.ok(team);
    }

    @PutMapping("/teams/{teamId}")
    public ResponseEntity<?> updateTeam(@PathVariable Long teamId,
                                        @RequestBody Map<String, String> body) {
        Team team = adminService.updateTeam(teamId, body.get("name"), body.get("flagImageUrl"));
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long teamId) {
        adminService.deleteTeam(teamId);
        return ResponseEntity.ok("Team deleted.");
    }

    @GetMapping("/teams")
    public ResponseEntity<?> getAllTeams() {
        return ResponseEntity.ok(adminService.getAllTeams());
    }

    @PostMapping("/players")
    public ResponseEntity<?> createPlayer(@RequestBody Map<String, Object> body) {
        String name = body.get("name").toString();
        Long teamId = Long.valueOf(body.get("teamId").toString());
        String playerImageUrl = body.get("playerImageUrl").toString();
        Player player = adminService.createPlayer(name, teamId,playerImageUrl);
        return ResponseEntity.ok(player);
    }

    @PutMapping("/players/{playerId}/goals")
    public ResponseEntity<?> updateGoals(@PathVariable Long playerId,
                                         @RequestBody Map<String, Object> body) {
        int goals = Integer.parseInt(body.get("goalsScored").toString());
        Player player = adminService.updatePlayerGoals(playerId, goals);
        return ResponseEntity.ok(player);
    }

    @DeleteMapping("/players/{playerId}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long playerId) {
        adminService.deletePlayer(playerId);
        return ResponseEntity.ok("Player deleted.");
    }

    @GetMapping("/players")
    public ResponseEntity<?> getAllPlayers() {
        return ResponseEntity.ok(adminService.getAllPlayers());
    }

    @PostMapping("/matches")
    public ResponseEntity<?> createMatch(@RequestBody Map<String, Object> body) {
        Long homeTeamId = Long.valueOf(body.get("homeTeamId").toString());
        Long awayTeamId = Long.valueOf(body.get("awayTeamId").toString());
        String stadium = body.get("stadium").toString();
        LocalDateTime matchDate = LocalDateTime.parse(body.get("matchDate").toString());
        Long underdogTeamId = body.get("underdogTeamId") != null
                ? Long.valueOf(body.get("underdogTeamId").toString())
                : null;

        Match match = adminService.createMatch(homeTeamId, awayTeamId, stadium, matchDate, underdogTeamId);
        return ResponseEntity.ok(match);
    }

    @PutMapping("/matches/{matchId}/score")
    public ResponseEntity<?> enterScore(@PathVariable Long matchId,
                                        @RequestBody Map<String, Object> body) {
        int homeScore = Integer.parseInt(body.get("homeScore").toString());
        int awayScore = Integer.parseInt(body.get("awayScore").toString());
        Match match = adminService.enterMatchScore(matchId, homeScore, awayScore);
        return ResponseEntity.ok(match);
    }

    @PutMapping("/matches/{matchId}/cards")
    public ResponseEntity<?> enterCards(@PathVariable Long matchId,
                                        @RequestBody Map<String, Object> body) {
        int totalCards = Integer.parseInt(body.get("totalCards").toString());
        Match match = adminService.enterMatchCards(matchId, totalCards);
        return ResponseEntity.ok(match);
    }

    @PostMapping("/calculate-top-scorer")
    public ResponseEntity<?> calculateTopScorer() {
        adminService.triggerTopScorerCalculation();
        return ResponseEntity.ok("Top scorer points calculated for all users.");
    }

    @PostMapping("/quiz")
    public ResponseEntity<Quiz> addQuiz(@RequestBody Map<String, String> body) {
        Quiz quiz = adminService.addQuiz(
                body.get("question"),
                body.get("correctAnswer"),
                body.get("wrongAnswers"),
                body.get("difficulty")
        );
        return ResponseEntity.ok(quiz);
    }
}
