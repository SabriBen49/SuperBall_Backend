package com.superball.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "predictions")
@Data
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private int homeScore;

    @Column(nullable = false)
    private int awayScore;

    private Integer totalCards;

    private int pointsEarned = 0;
}
