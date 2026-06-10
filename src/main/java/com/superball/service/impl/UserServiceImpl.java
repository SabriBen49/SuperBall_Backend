package com.superball.service.impl;

import com.superball.entity.User;
import com.superball.repository.PredictionRepository;
import com.superball.repository.TopScorerPredictionRepository;
import com.superball.repository.UserRepository;
import com.superball.service.UserService;
import com.superball.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public User register(String nickname, String email, String password, String profileImageUrl) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new RuntimeException("Nickname already taken");
        }

        User user = new User();
        user.setNickname(nickname);
        user.setEmail(email);
        user.setProfileImageUrl(profileImageUrl);
        user.setPassword(passwordEncoder.encode(password));
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);
        sendVerificationEmail(user);
        return user;
    }

    private void sendVerificationEmail(User user) {
        String link = baseUrl + "/api/auth/verify?token=" + user.getVerificationToken();

        String html = "<div style='font-family:sans-serif;max-width:480px;margin:auto'>"
                + "<h2>Welcome to Superball! ⚽</h2>"
                + "<p>Click the button below to verify your email address.</p>"
                + "<a href='" + link + "' style='display:inline-block;padding:12px 24px;"
                + "background:#dc2626;color:white;text-decoration:none;"
                + "border-radius:6px;font-weight:600'>Verify my account</a>"
                + "<p style='color:#888;font-size:12px;margin-top:24px'>"
                + "If you didn't create an account, you can ignore this email.</p>"
                + "</div>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "Superball", "email", "superball.wc.2026@gmail.com"));
        body.put("to", List.of(Map.of("email", user.getEmail(), "name", user.getNickname())));
        body.put("subject", "Superball – Verify your email");
        body.put("htmlContent", html);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email", request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Brevo returned: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(
                user.getId(),
                user.getRole(),
                user.getProfileImageUrl(),
                user.getNickname(),
                user.getQuizPoints()
        );
    }

    @Override
    public User changeNickname(Long userId, String newNickname) {
        if (userRepository.existsByNickname(newNickname)) {
            throw new RuntimeException("Nickname already taken");
        }
        User user = findById(userId);
        user.setNickname(newNickname);
        return userRepository.save(user);
    }

    @Override
    public User changeProfileImage(Long userId, String imageUrl) {
        User user = findById(userId);
        user.setProfileImageUrl(imageUrl);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        predictionRepository.deleteByUserId(userId);
        topScorerPredictionRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public int getTotalPoints(Long userId) {
        User user = findById(userId);

        int predictionPoints = predictionRepository.findByUserId(userId)
                .stream()
                .mapToInt(p -> p.getPointsEarned())
                .sum();

        int topScorerPoints = topScorerPredictionRepository.findByUserId(userId)
                .map(ts -> ts.getPointsEarned())
                .orElse(0);

        return user.getQuizPoints() + predictionPoints + topScorerPoints;
    }
}
