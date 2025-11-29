package com.example.AR_BE.domain.request;

import java.util.List;
import lombok.Data;

@Data
public class UpdateProductDTO {
    private String name;
    private Double oldPrice;
    private Double saleRate;
    private Integer quantity;
    private String description;
    private List<String> imageUrl;
    private Long categoryId;
}
