package com.superball.service;

import com.superball.entity.Match;
import com.superball.entity.Player;
import com.superball.entity.Quiz;
import com.superball.entity.Team;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {

    Team createTeam(String name, String flagImageUrl);
    Team updateTeam(Long teamId, String name, String flagImageUrl);
    void deleteTeam(Long teamId);
    List<Team> getAllTeams();

    Quiz addQuiz(String question, String correctAnswer, String wrongAnswers, String difficulty);

    Player createPlayer(String name, Long teamId,String playerImageUrl);
    Player updatePlayerGoals(Long playerId, int goalsScored);
    void deletePlayer(Long playerId);
    List<Player> getAllPlayers();

    Match createMatch(Long homeTeamId, Long awayTeamId, String stadium, LocalDateTime matchDate, Long underdogTeamId);

    Match enterMatchScore(Long matchId, int homeScore, int awayScore);

    Match enterMatchCards(Long matchId, int totalCards);

    List<Match> getAllMatches();

    void triggerTopScorerCalculation();

    List<Map<String, Object>> getLeaderboard();

    List<Player> getTopScorerLeaderboard();
}
