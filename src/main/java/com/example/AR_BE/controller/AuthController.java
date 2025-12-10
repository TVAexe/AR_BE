package com.example.AR_BE.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.AR_BE.domain.User;
import com.example.AR_BE.domain.request.RestLoginDTORequest;
import com.example.AR_BE.domain.response.NewUserDTOResponse;
import com.example.AR_BE.domain.response.RestLoginDTOResponse;
import com.example.AR_BE.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.AR_BE.utils.SecurityUtils;
import com.example.AR_BE.utils.exception.IdInvalidException;

@RestController
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtils securityUtils;
    private final UserService userService;
    @Value("${demo.jwt.refresh-token-validity-in-seconds}")
    private Long jwtRefreshTokenValidity;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtils securityUtils,
            UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtils = securityUtils;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<RestLoginDTOResponse> login(@Valid @RequestBody RestLoginDTORequest loginDTO) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        RestLoginDTOResponse restLoginDTO = new RestLoginDTOResponse();
        User userDB = userService.findUserByUsername(loginDTO.getEmail());
        RestLoginDTOResponse.UserLogin userLogin = new RestLoginDTOResponse.UserLogin(userDB.getId(), userDB.getEmail(),
                userDB.getName(), userDB.getRole(), userDB.getPhoneNumber());
        restLoginDTO.setUser(userLogin);
        String accessToken = this.securityUtils.createAccessToken(authentication, restLoginDTO.getUser());
        restLoginDTO.setAccessToken(accessToken);
        String refreshToken = this.securityUtils.createRefreshToken(userDB.getEmail(), restLoginDTO);
        this.userService.updateUserToken(refreshToken, userDB.getEmail());
        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken).httpOnly(true).secure(true)
                .path("/").maxAge(jwtRefreshTokenValidity).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(restLoginDTO);
    }

    @GetMapping("/auth/account")
    public ResponseEntity<RestLoginDTOResponse.UserLogin> getAccount() {
        String email = SecurityUtils.getCurrentUserLogin().isPresent() ? SecurityUtils.getCurrentUserLogin().get() : "";
        User userDB = userService.findUserByUsername(email);
        RestLoginDTOResponse.UserLogin userLogin = new RestLoginDTOResponse.UserLogin(userDB.getId(), userDB.getEmail(),
                userDB.getName(), userDB.getRole(), userDB.getPhoneNumber());
        return ResponseEntity.ok().body(userLogin);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<NewUserDTOResponse> register(@Valid @RequestBody User requestUser)
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