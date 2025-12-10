package com.example.AR_BE.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {
    
    @NotBlank(message = "Status không được để trống")
    private String status;
}