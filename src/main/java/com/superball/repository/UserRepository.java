package com.superball.repository;

import com.superball.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByVerificationToken(String token);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);
}

