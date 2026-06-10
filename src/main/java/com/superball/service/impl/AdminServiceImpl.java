package com.superball.service.impl;

import com.superball.entity.*;
import com.superball.repository.*;
import com.superball.service.AdminService;
import com.superball.service.PredictionService;
import com.superball.service.TopScorerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;
    private final UserRepository userRepository;
    private final TopScorerService topScorerService;
    private final PredictionService predictionService;
    private final QuizRepository quizRepository;

    private static final Set<String> VALID_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");

    @Override
    public Team createTeam(String name, String flagImageUrl) {
        if (teamRepository.existsByName(name)) {
            throw new RuntimeException("Team already exists");
        }
        Team team = new Team();
        team.setName(name);
        team.setFlagImageUrl(flagImageUrl);
        return teamRepository.save(team);
    }

    @Override
    public Team updateTeam(Long teamId, String name, String flagImageUrl) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        team.setName(name);
        team.setFlagImageUrl(flagImageUrl);
        return teamRepository.save(team);
    }

    @Override
    public void deleteTeam(Long teamId) {
        teamRepository.deleteById(teamId);
    }

    @Override
    public List<Team> getAllTeams() {
        return teamRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Player createPlayer(String name, Long teamId,String playerImageUrl) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Player player = new Player();
        player.setName(name);
        player.setTeam(team);
        player.setPlayerImageUrl(playerImageUrl);
        return playerRepository.save(player);
    }

    @Override
    public Player updatePlayerGoals(Long playerId, int goalsScored) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        player.setGoalsScored(goalsScored);
        return playerRepository.save(player);
    }

    @Override
    public void deletePlayer(Long playerId) {
        playerRepository.deleteById(playerId);
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public Match createMatch(Long homeTeamId, Long awayTeamId, String stadium, LocalDateTime matchDate, Long underdogTeamId) {
        Team homeTeam = teamRepository.findById(homeTeamId)
                .orElseThrow(() -> new RuntimeException("Home team not found"));
        Team awayTeam = teamRepository.findById(awayTeamId)
                .orElseThrow(() -> new RuntimeException("Away team not found"));
        Match match = new Match();
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setStadium(stadium);
        match.setMatchDate(matchDate);
        match.setUnderdogTeamId(underdogTeamId);
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public Match enterMatchScore(Long matchId, int homeScore, int awayScore) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setResultEntered(true);
        matchRepository.save(match);
        predictionService.calculatePointsForMatch(matchId);
        return match;
    }

    @Override
    @Transactional
    public Match enterMatchCards(Long matchId, int totalCards) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        match.setTotalCards(totalCards);
        match.setCardsEntered(true);
        matchRepository.save(match);
        predictionService.calculatePointsForMatch(matchId);
        return match;
    }

    @Override
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @Override
    public void triggerTopScorerCalculation() {
        topScorerService.calculateTopScorerPoints();
    }

    @Override
    public List<Map<String, Object>> getLeaderboard() {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());
        List<Prediction> allPredictions = predictionRepository.findAll();
        List<TopScorerPrediction> allTopScorer = topScorerPredictionRepository.findAll();
        Map<Long, Integer> predictionPoints = allPredictions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getUser().getId(),
                        Collectors.summingInt(Prediction::getPointsEarned)
                ));
        Map<Long, Integer> correctCounts = allPredictions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getUser().getId(),
                        Collectors.summingInt(p -> p.getPointsEarned() > 0 ? 1 : 0)
                ));
        Map<Long, Integer> topScorerPoints = allTopScorer.stream()
                .collect(Collectors.toMap(
                        ts -> ts.getUser().getId(),
                        TopScorerPrediction::getPointsEarned
                ));

        return users.stream()
                .map(user -> {
                    int pred = predictionPoints.getOrDefault(user.getId(), 0);
                    int ts = topScorerPoints.getOrDefault(user.getId(), 0);
                    int total = pred + user.getQuizPoints() + ts;
                    int correct = correctCounts.getOrDefault(user.getId(), 0);

                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("nickname", user.getNickname());
                    entry.put("totalPoints", total);
                    entry.put("correctPredictions", correct);
                    entry.put("profileImageUrl", user.getProfileImageUrl());
                    return entry;
                })
                .sorted((a, b) -> Integer.compare(
                        (Integer) b.get("totalPoints"),
                        (Integer) a.get("totalPoints")
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Player> getTopScorerLeaderboard() {
        return playerRepository.findAllByOrderByGoalsScoredDesc();
    }

    @Override
    public Quiz addQuiz(String question, String correctAnswer, String wrongAnswers, String difficulty) {
        String difficultyUpper = difficulty.toUpperCase();
        if (!VALID_DIFFICULTIES.contains(difficultyUpper)) {
            throw new RuntimeException("Invalid difficulty. Must be EASY, MEDIUM, or HARD.");
        }
        if (question == null || question.isBlank()) throw new RuntimeException("Question must not be empty.");
        if (correctAnswer == null || correctAnswer.isBlank()) throw new RuntimeException("Correct answer must not be empty.");
        if (wrongAnswers == null || wrongAnswers.isBlank()) throw new RuntimeException("Wrong answers must not be empty.");

        Quiz quiz = new Quiz();
        quiz.setQuestion(question.trim());
        quiz.setCorrectAnswer(correctAnswer.trim());
        quiz.setWrongAnswers(wrongAnswers.trim());
        quiz.setDifficulty(difficultyUpper);
        return quizRepository.save(quiz);
    }
}
