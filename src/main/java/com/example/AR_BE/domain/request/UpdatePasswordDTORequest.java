package com.example.AR_BE.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdatePasswordDTORequest {
    @NotBlank(message = "Password cũ không được để trống")
    private String oldPassword;

    @NotBlank(message = "Password mới không được để trống")
    private String newPassword;
}
