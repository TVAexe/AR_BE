package com.example.AR_BE.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FetchUserDTOResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Instant createdAt;
    private Instant updatedAt;
}
