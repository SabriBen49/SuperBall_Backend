package com.superball.repository;

import com.superball.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);
    List<Team> findAllByOrderByNameAsc();
    boolean existsByName(String name);
}

