package com.example.AR_BE.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
    private Long productId;
    private String productName;
    private String productType;
    private Integer quantity;
    private Double priceAtPurchase;
}
