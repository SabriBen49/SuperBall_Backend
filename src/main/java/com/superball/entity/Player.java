package com.superball.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "players")
@Data
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int goalsScored = 0;

    @Column(nullable = false)
    private String playerImageUrl;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
