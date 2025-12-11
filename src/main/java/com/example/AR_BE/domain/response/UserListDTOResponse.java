package com.example.AR_BE.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTOResponse {
    private long id;
    private String name;
    private String email;
    private String phoneNumber;
    private int age;
    private String gender;
    private RoleResponse role;
    private Instant createdAt;
    private String createdBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleResponse {
        private long id;
        private String name;
    }
}