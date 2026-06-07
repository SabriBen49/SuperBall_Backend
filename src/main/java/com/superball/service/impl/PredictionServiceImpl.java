package com.superball.service.impl;

import com.superball.entity.Match;
import com.superball.entity.Prediction;
import com.superball.entity.User;
import com.superball.repository.MatchRepository;
import com.superball.repository.PredictionRepository;
import com.superball.repository.UserRepository;
import com.superball.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Override
    public Prediction createPrediction(Long userId, Long matchId, int homeScore, int awayScore, Integer totalCards) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (LocalDateTime.now().isAfter(match.getMatchDate().minusHours(2))) {
            throw new RuntimeException("Predictions are locked within 2 hours of kick-off");
        }

        if (predictionRepository.findByUserIdAndMatchId(userId, matchId).isPresent()) {
            throw new RuntimeException("You already have a prediction for this match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Prediction prediction = new Prediction();
        prediction.setUser(user);
        prediction.setMatch(match);
        prediction.setHomeScore(homeScore);
        prediction.setAwayScore(awayScore);
        prediction.setTotalCards(totalCards);

        return predictionRepository.save(prediction);
    }

    @Override
    public Prediction updatePrediction(Long predictionId, Long userId, int homeScore, int awayScore, Integer totalCards) {
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        if (!prediction.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own predictions");
        }

        if (LocalDateTime.now().isAfter(prediction.getMatch().getMatchDate().minusHours(2))) {
            throw new RuntimeException("Cannot modify prediction less than 2 hours before match");
        }

        prediction.setHomeScore(homeScore);
        prediction.setAwayScore(awayScore);
        prediction.setTotalCards(totalCards);

        return predictionRepository.save(prediction);
    }

    @Override
    public List<Prediction> getUserPredictions(Long userId) {
        return predictionRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void calculatePointsForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        List<Prediction> predictions = predictionRepository.findByMatchId(matchId);

        for (Prediction prediction : predictions) {
            int points = 0;

            if (match.isResultEntered()) {
                int actualHome = match.getHomeScore();
                int actualAway = match.getAwayScore();
                int predictedHome = prediction.getHomeScore();
                int predictedAway = prediction.getAwayScore();

                if (predictedHome == actualHome && predictedAway == actualAway) {
                    points += 5;
                } else if (sameOutcome(predictedHome, predictedAway, actualHome, actualAway)) {
                    points += 3;
                }

                if (match.getUnderdogTeamId() != null) {
                    boolean underdogIsHome = match.getUnderdogTeamId().equals(match.getHomeTeam().getId());
                    boolean underdogActuallyWon = underdogIsHome ? actualHome > actualAway : actualAway > actualHome;
                    boolean userPredictedUnderdogWin = underdogIsHome ? predictedHome > predictedAway : predictedAway > predictedHome;

                    if (underdogActuallyWon && userPredictedUnderdogWin) {
                        points += 3;
                    }
                }
            }

            if (match.isCardsEntered() && prediction.getTotalCards() != null) {
                if (prediction.getTotalCards().equals(match.getTotalCards())) {
                    points += 2;
                }
            }

            prediction.setPointsEarned(points);
        }

        predictionRepository.saveAll(predictions);
    }

    private boolean sameOutcome(int predictedHome, int predictedAway, int actualHome, int actualAway) {
        return Integer.compare(predictedHome, predictedAway) == Integer.compare(actualHome, actualAway);
    }
}
