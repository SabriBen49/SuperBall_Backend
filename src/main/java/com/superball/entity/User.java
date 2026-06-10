package com.superball.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String profileImageUrl;

    private boolean verified = false;

    private String verificationToken;

    private String role = "USER";

    private LocalDateTime lastQuizPlayedAt;

    private int quizPoints = 0;

    @ElementCollection
    @CollectionTable(name = "user_quiz_played", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "quiz_id")
    private List<Long> listOfQuizPlayed = new ArrayList<>();
}
