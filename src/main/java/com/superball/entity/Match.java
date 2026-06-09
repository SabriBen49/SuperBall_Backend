package com.superball.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    private String stadium;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    private Long underdogTeamId;

    private Integer homeScore;
    private Integer awayScore;
    private Integer totalCards;

    private boolean resultEntered = false;
    private boolean cardsEntered = false;
}
