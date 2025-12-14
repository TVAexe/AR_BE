package com.example.AR_BE.domain.dto;

import com.example.AR_BE.domain.ProductARModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private Double oldPrice;
    private Double saleRate;
    private int quantity;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<String> imageUrl;
    private ProductARModelDTO arModel;
    private CategoryDTO category;
}
