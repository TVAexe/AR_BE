package com.example.AR_BE.domain.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailDTO {
    private Long orderId;
    private String shippingAddress;
    private Double totalAmount;
    private String status;
    private List<OrderItemDetailDTO> items;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}

