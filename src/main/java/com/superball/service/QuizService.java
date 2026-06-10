package com.superball.service;

import com.superball.entity.Quiz;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface QuizService {

    ResponseEntity<?> checkCooldown(Long userId);

    Quiz getQuizForUser(Long userId, String difficulty);

    Map<String, Object> submitAnswerWithDetails(Long userId, Long quizId, String userAnswer);
}
