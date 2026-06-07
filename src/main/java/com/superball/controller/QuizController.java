package com.superball.controller;

import com.superball.entity.Quiz;
import com.superball.service.QuizService;
import com.superball.service.UserService;
import com.superball.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping("/check-cooldown")
    public ResponseEntity<?> checkCooldown(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader);
        return quizService.checkCooldown(userId);
    }

    @GetMapping("/play")
    public ResponseEntity<?> getQuiz(@RequestHeader("Authorization") String authHeader,
                                     @RequestParam String difficulty) {
        Long userId = jwtUtil.extractUserId(authHeader);
        int cost=0;
        switch (difficulty) {
            case "EASY" -> cost=5;
            case "MEDIUM" -> cost=10;
            case "HARD" -> cost=15;

        }

        if(userService.getTotalPoints(userId)<cost){
            return ResponseEntity
                    .badRequest()
                    .body("You don't have enough points");
        }

        Quiz quiz = quizService.getQuizForUser(userId, difficulty);

        List<String> answers = new ArrayList<>(Arrays.asList(quiz.getWrongAnswers().split("-")));
        answers.add(quiz.getCorrectAnswer());
        Collections.shuffle(answers);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("quizId", quiz.getId());
        response.put("question", quiz.getQuestion());
        response.put("difficulty", quiz.getDifficulty());
        response.put("answers", answers);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody Map<String, Object> body) {
        Long userId = jwtUtil.extractUserId(authHeader);
        Long quizId = Long.valueOf(body.get("quizId").toString());
        String answer = body.get("answer").toString();

        Map<String, Object> response = quizService.submitAnswerWithDetails(userId, quizId, answer);
        return ResponseEntity.ok(response);
    }
}
