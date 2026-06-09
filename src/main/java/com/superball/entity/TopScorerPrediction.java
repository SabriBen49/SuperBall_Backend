package com.superball.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "top_scorer_predictions")
@Data
public class TopScorerPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private int pointsEarned = 0;
}
