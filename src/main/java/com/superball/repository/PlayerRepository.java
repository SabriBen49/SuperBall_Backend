package com.superball.repository;

import com.superball.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);
    List<Player> findTop3ByOrderByGoalsScoredDesc();
    List<Player> findAllByOrderByGoalsScoredDesc();
}

