package com.superball.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.superball.entity.User;
import com.superball.repository.PredictionRepository;
import com.superball.repository.TopScorerPredictionRepository;
import com.superball.repository.UserRepository;
import com.superball.service.UserService;
import com.superball.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;
    private final TopScorerPredictionRepository topScorerPredictionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    @Value("${app.base-url}")
    private String baseUrl;


    @Value("${resend.api.key}")
    private String resendApiKey;


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
        Resend resend = new Resend(resendApiKey);
        String link = baseUrl + "/api/auth/verify?token=" + user.getVerificationToken();

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(user.getEmail())
                .subject("Superball - Verify your email")
                .html("<p>Click <a href='" + link + "'>here</a> to verify your account.</p>")
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send verification email", e);
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
