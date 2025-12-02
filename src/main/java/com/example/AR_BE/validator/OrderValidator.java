package com.example.AR_BE.validator;

import org.springframework.stereotype.Component;

import com.example.AR_BE.domain.request.CreateOrderRequest;
import com.example.AR_BE.domain.request.OrderItemRequest;

@Component
public class OrderValidator {

    public void validateRequest(CreateOrderRequest req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Items must not be empty");
        }
    }

    public void validateItem(OrderItemRequest itemReq) {
        if (itemReq.getProductId() == null || itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid product or quantity");
        }
    }
}

