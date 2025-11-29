package com.example.AR_BE.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.AR_BE.domain.dto.OrderDTO;
import com.example.AR_BE.domain.dto.OrderDetailDTO;
import com.example.AR_BE.domain.request.CreateOrderRequest;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.domain.Order;
import com.example.AR_BE.service.OrderService;
import com.example.AR_BE.utils.constants.StatusEnum;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(request);
        OrderDTO dto = orderService.convertToDTO(order);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/orders/paging")
    public ResponseEntity<ResultPaginationDTO> getMyOrdersPaging(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResultPaginationDTO result = orderService.getOrdersByStatusPaging(status, page, size);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId) {

        OrderDTO canceledOrder = orderService.cancelOrder(orderId);

        return ResponseEntity.ok(canceledOrder);
    }

    @GetMapping("/orders/{orderId}/detail")
    public ResponseEntity<OrderDetailDTO> getOrderDetail(
            @PathVariable Long orderId) {

        OrderDetailDTO dto = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(dto);
    }

}
