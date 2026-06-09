package com.superball.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quizzes")
@Data
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String correctAnswer;

    @Column(nullable = false)
    private String wrongAnswers;

    @Column(nullable = false)
    private String difficulty;
}
