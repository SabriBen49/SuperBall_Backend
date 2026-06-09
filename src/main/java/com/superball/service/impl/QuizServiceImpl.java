package com.superball.service.impl;

import com.superball.entity.Quiz;
import com.superball.entity.User;
import com.superball.entity.Prediction;
import com.superball.repository.PredictionRepository;
import com.superball.repository.QuizRepository;
import com.superball.repository.UserRepository;
import com.superball.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;

    private static final int EASY_POINTS   = 5;
    private static final int MEDIUM_POINTS = 10;
    private static final int HARD_POINTS   = 15;

    @Override
    public ResponseEntity<?> checkCooldown(Long userId) {
        User user = findUser(userId);
        if (isCooldownActive(user)) {
            LocalDateTime nextAllowed = user.getLastQuizPlayedAt().plusHours(24);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Cooldown active — you can play your next quiz at " + nextAllowed);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
        }
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @Override
    public Quiz getQuizForUser(Long userId, String difficulty) {
        User user = findUser(userId);
        assertNoCooldown(user);

        int stake = getStake(difficulty);
        int totalPoints = getUserTotalPoints(user);
        if (totalPoints < stake) {
            throw new RuntimeException("Not enough points to play " + difficulty + " (need " + stake + ", you have " + totalPoints + ")");
        }

        List<Long> played = user.getListOfQuizPlayed();

        if (played.isEmpty()) {
            return quizRepository.findRandomByDifficulty(difficulty.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("No quizzes available for this difficulty"));
        } else {
            return quizRepository.findRandomUnplayedByDifficulty(difficulty.toUpperCase(), played)
                    .orElseThrow(() -> new RuntimeException("You have played all quizzes for this difficulty"));
        }
    }

    @Override
    public Map<String, Object> submitAnswerWithDetails(Long userId, Long quizId, String userAnswer) {
        User user = findUser(userId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (user.getListOfQuizPlayed().contains(quizId)) {
            throw new RuntimeException("You have already played this quiz");
        }

        int stake = getStake(quiz.getDifficulty());
        boolean correct = quiz.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());

        user.setQuizPoints(user.getQuizPoints() + (correct ? stake : -stake));
        user.getListOfQuizPlayed().add(quizId);
        user.setLastQuizPlayedAt(LocalDateTime.now());
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("result", correct ? "CORRECT" : "WRONG");
        if (!correct) {
            response.put("correctAnswer", quiz.getCorrectAnswer());
        }
        return response;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void assertNoCooldown(User user) {
        if (isCooldownActive(user)) {
            LocalDateTime nextAllowed = user.getLastQuizPlayedAt().plusHours(24);
            throw new RuntimeException("Cooldown active — you can play your next quiz at " + nextAllowed);
        }
    }

    private boolean isCooldownActive(User user) {
        return user.getLastQuizPlayedAt() != null &&
               LocalDateTime.now().isBefore(user.getLastQuizPlayedAt().plusHours(24));
    }

    private int getStake(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY"   -> EASY_POINTS;
            case "MEDIUM" -> MEDIUM_POINTS;
            case "HARD"   -> HARD_POINTS;
            default -> throw new RuntimeException("Invalid difficulty: " + difficulty);
        };
    }

    private int getUserTotalPoints(User user) {
        int predictionPoints = predictionRepository.findByUserId(user.getId())
                .stream()
                .mapToInt(Prediction::getPointsEarned)
                .sum();
        return user.getQuizPoints() + predictionPoints;
    }
}
