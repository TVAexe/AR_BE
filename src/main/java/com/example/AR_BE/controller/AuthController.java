package com.example.AR_BE.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.AR_BE.domain.User;
import com.example.AR_BE.domain.request.RestLoginDTORequest;
import com.example.AR_BE.domain.response.RestLoginDTOResponse;
import com.example.AR_BE.domain.response.RestLoginDTOResponse.UserLogin;
import com.example.AR_BE.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.AR_BE.utils.SecurityUtils;

@RestController
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtils securityUtils;
    private final UserService userService;
    @Value("${demo.jwt.refresh-token-validity-in-seconds}")
    private Long jwtRefreshTokenValidity;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtils securityUtils,
            UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtils = securityUtils;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<RestLoginDTOResponse> login(@Valid @RequestBody RestLoginDTORequest loginDTO) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        RestLoginDTOResponse restLoginDTO = new RestLoginDTOResponse();
        User userDB = userService.findUserByUsername(loginDTO.getUsername());
        RestLoginDTOResponse.UserLogin userLogin = new RestLoginDTOResponse.UserLogin(userDB.getId(), userDB.getEmail(), userDB.getName());
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
        RestLoginDTOResponse.UserLogin userLogin = new RestLoginDTOResponse.UserLogin(userDB.getId(), userDB.getEmail(), userDB.getName());
        return ResponseEntity.ok().body(userLogin);
    }
    

}