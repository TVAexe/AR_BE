package com.example.AR_BE.domain.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductDTO {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotNull(message = "Giá không được để trống")
    private Double oldPrice;

    private Double saleRate;

    @NotNull(message = "Số lượng không được để trống")
    private Integer quantity;

    private String description;

    private MultipartFile[] images;

    private MultipartFile modelFile;

    @NotNull(message = "Category không được để trống")
    private Long categoryId;
}
