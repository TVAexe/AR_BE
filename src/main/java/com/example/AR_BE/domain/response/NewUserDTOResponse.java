package com.example.AR_BE.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewUserDTOResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Instant createdAt;
}
