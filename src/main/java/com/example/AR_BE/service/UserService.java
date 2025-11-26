package com.example.AR_BE.service;

import com.example.AR_BE.domain.request.UpdatePasswordDTORequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.AR_BE.domain.User;
import com.example.AR_BE.domain.response.NewUserDTOResponse;
import com.example.AR_BE.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    public NewUserDTOResponse convertToNewUserDTOResponse(User user) {
        NewUserDTOResponse newUserDTOResponse = new NewUserDTOResponse();
        newUserDTOResponse.setId(user.getId());
        newUserDTOResponse.setEmail(user.getEmail());
        newUserDTOResponse.setName(user.getName());
        newUserDTOResponse.setPhoneNumber(user.getPhoneNumber());
        newUserDTOResponse.setCreatedAt(user.getCreatedAt());
        return newUserDTOResponse;
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User handleGetUser(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.orElse(null);
    }

    public void changePasswordByEmail(String email, UpdatePasswordDTORequest req) {
        User user = userRepository.findByEmail(email);
        if (user == null) return;

        if (req.getOldPassword() != null &&
                !passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong old password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}
