package com.superball.service.impl;

import com.superball.entity.Match;
import com.superball.entity.Player;
import com.superball.entity.TopScorerPrediction;
import com.superball.entity.User;
import com.superball.repository.MatchRepository;
import com.superball.repository.PlayerRepository;
import com.superball.repository.TopScorerPredictionRepository;
import com.superball.repository.UserRepository;
import com.superball.service.TopScorerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopScorerServiceImpl implements TopScorerService {

    private final TopScorerPredictionRepository topScorerPredictionRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Override
    public TopScorerPrediction savePrediction(Long userId, Long playerId) {
        Match firstMatch = matchRepository.findFirstByOrderByMatchDateAsc()
                .orElseThrow(() -> new RuntimeException("No matches found in the tournament"));

        if (LocalDateTime.now().isAfter(firstMatch.getMatchDate().minusHours(2))) {
            throw new RuntimeException("Top scorer prediction is locked — less than 2 hours before first match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        TopScorerPrediction prediction = topScorerPredictionRepository.findByUserId(userId)
                .orElse(new TopScorerPrediction());

        prediction.setUser(user);
        prediction.setPlayer(player);
        prediction.setPointsEarned(0);

        return topScorerPredictionRepository.save(prediction);
    }

    @Override
    public TopScorerPrediction getUserPrediction(Long userId) {
        return topScorerPredictionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No top scorer prediction found for this user"));
    }

    @Override
    @Transactional
    public void calculateTopScorerPoints() {
        List<Player> top3 = playerRepository.findTop3ByOrderByGoalsScoredDesc();

        if (top3.isEmpty()) {
            throw new RuntimeException("No players with goals found");
        }

        int maxGoals = top3.get(0).getGoalsScored();
        int top3MinGoals = top3.get(top3.size() - 1).getGoalsScored();

        List<TopScorerPrediction> allPredictions = topScorerPredictionRepository.findAll();

        for (TopScorerPrediction prediction : allPredictions) {
            int goals = prediction.getPlayer().getGoalsScored();

            if (goals == maxGoals) {
                prediction.setPointsEarned(10);
            } else if (goals >= top3MinGoals) {
                prediction.setPointsEarned(3);
            } else {
                prediction.setPointsEarned(0);
            }
        }

        topScorerPredictionRepository.saveAll(allPredictions);
    }
}
