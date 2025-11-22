package com.example.AR_BE.service;

import org.springframework.stereotype.Service;

import com.example.AR_BE.domain.User;
import com.example.AR_BE.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {
        return userRepository.save(user);
    }

    public boolean checkEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkPhoneExist(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User findUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public void updateUserToken(String token, String email) {
        User user = this.findUserByUsername(email);
        if (user != null) {
            user.setRefreshToken(token);
            this.userRepository.save(user);
        }
    }
}
