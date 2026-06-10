package com.superball.service;

import com.superball.entity.User;

public interface UserService {

    User register(String nickname, String email, String password, String imageUrl);

    void verifyEmail(String token);

    String login(String email, String password);

    User changeNickname(Long userId, String newNickname);

    User changeProfileImage(Long userId, String imageUrl);

    void deleteAccount(Long userId);

    User findById(Long userId);

    int getTotalPoints(Long userId);
}
