package com.example.AR_BE.domain.dto;

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
}

