package com.superball.repository;

import com.superball.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    
    Optional<Match> findFirstByOrderByMatchDateAsc();

    
    Optional<Match> findFirstByOrderByMatchDateDesc();
}

