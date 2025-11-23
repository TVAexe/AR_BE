package com.example.AR_BE.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class RestLoginDTORequest {
    @Getter
    @Setter
    @NotBlank(message = "email không được để trống")
    private String email;

    @Getter
    @Setter
    @NotBlank(message = "Password không được để trống")
    private String password;
}
