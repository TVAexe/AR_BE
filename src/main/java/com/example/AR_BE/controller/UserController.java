package com.example.AR_BE.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.AR_BE.domain.User;
import com.example.AR_BE.domain.response.NewUserDTOResponse;
import com.example.AR_BE.service.UserService;

import jakarta.validation.Valid;
import com.example.AR_BE.utils.exception.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<NewUserDTOResponse> createNewUser(@Valid @RequestBody User requestUser)
            throws IdInvalidException {
        boolean isEmailExisted = this.userService.checkEmailExist(requestUser.getEmail());
        if (isEmailExisted) {
            throw new IdInvalidException("Email " + requestUser.getEmail() + " already exists");
        }

        boolean isPhoneExisted = this.userService.checkPhoneExist(requestUser.getPhoneNumber());
        if (isPhoneExisted) {
            throw new IdInvalidException("Phone number " + requestUser.getPhoneNumber() + " already exists");
        }

        String hashedPassword = this.passwordEncoder.encode(requestUser.getPassword());
        requestUser.setPassword(hashedPassword);
        User newUser = this.userService.handleCreateUser(requestUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToNewUserDTOResponse(newUser));
    }
}
