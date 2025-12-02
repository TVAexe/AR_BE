package com.example.AR_BE.controller;

import com.example.AR_BE.domain.request.UpdatePasswordDTORequest;
import com.example.AR_BE.utils.SecurityUtils;
import com.example.AR_BE.utils.annotation.ApiMessage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.AR_BE.domain.User;
import com.example.AR_BE.domain.response.NewUserDTOResponse;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.domain.response.UpdateUserDTOResponse;
import com.example.AR_BE.service.UserService;

import jakarta.validation.Valid;
import com.example.AR_BE.utils.exception.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

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

        String hashedPassword = this.passwordEncoder.encode(requestUser.getPassword());
        requestUser.setPassword(hashedPassword);
        User newUser = this.userService.handleCreateUser(requestUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToNewUserDTOResponse(newUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Deleted User")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws IdInvalidException {
        User currentUser = this.userService.handleGetUser(id);
        if (currentUser == null) {
            throw new IdInvalidException("User voi id: " + id + " khong ton tai");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/users/password")
    public ResponseEntity<?> changePassword(@RequestBody UpdatePasswordDTORequest req) {
        String email = SecurityUtils.getCurrentUserLogin().orElseThrow();
        userService.changePasswordByEmail(email, req);
        return ResponseEntity.ok("Password updated successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(@Filter Specification<User> filter, Pageable pageable) {
        return ResponseEntity.ok(this.userService.handleGetAllUsers(filter, pageable));
    }

    @PutMapping("users")
    public ResponseEntity<UpdateUserDTOResponse> updateUser(@RequestBody User user) throws IdInvalidException {
        User updatedUser = this.userService.handleUpdateUser(user);
        if (updatedUser == null) {
            throw new IdInvalidException("User voi id: " + user.getId() + " khong ton tai");
        }
        return ResponseEntity.ok(this.userService.convertUpdateUserDTO(updatedUser));
    }
}
