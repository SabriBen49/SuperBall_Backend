package com.superball.repository;

import com.superball.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    
    @Query(value = "SELECT * FROM quizzes q " +
                   "WHERE q.difficulty = :difficulty " +
                   "AND q.id NOT IN (:playedIds) " +
                   "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Quiz> findRandomUnplayedByDifficulty(@Param("difficulty") String difficulty,
                                                   @Param("playedIds") List<Long> playedIds);

    
    @Query(value = "SELECT * FROM quizzes q " +
                   "WHERE q.difficulty = :difficulty " +
                   "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Quiz> findRandomByDifficulty(@Param("difficulty") String difficulty);
}

