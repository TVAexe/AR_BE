package com.example.AR_BE.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductARModelDTO {
    private Long id;
    private String glbUrl;
    private Float scaleX;
    private Float scaleY;
    private Float scaleZ;
    private Float rotationY;
    private Boolean isArEnabled;
}
