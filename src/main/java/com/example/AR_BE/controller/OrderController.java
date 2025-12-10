package com.example.AR_BE.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.AR_BE.domain.dto.OrderDTO;
import com.example.AR_BE.domain.dto.OrderDetailDTO;
import com.example.AR_BE.domain.request.CreateOrderRequest;
import com.example.AR_BE.domain.request.UpdateOrderStatusRequest;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.domain.Order;
import com.example.AR_BE.service.OrderService;
import com.example.AR_BE.utils.annotation.ApiMessage;

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


    @GetMapping("/orders/my-orders")
    public ResponseEntity<ResultPaginationDTO> getMyOrders(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResultPaginationDTO result = orderService.getMyOrdersByStatus(status, page, size);
        return ResponseEntity.ok(result);
    }

    // API cho ADMIN - xem tất cả đơn hàng
    @GetMapping("/orders/admin/all-orders")
    public ResponseEntity<ResultPaginationDTO> getAllOrdersForAdmin(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResultPaginationDTO result = orderService.getAllOrdersByStatusForAdmin(status, page, size);
        return ResponseEntity.ok(result);
    }


    // API cho USER - Hủy đơn của chính mình (chỉ khi PENDING)
    @PutMapping("/orders/my-order/{orderId}/cancel")
    @ApiMessage("Order cancelled successfully")
    public ResponseEntity<OrderDTO> cancelMyOrder(@PathVariable Long orderId) {
        OrderDTO canceledOrder = orderService.cancelMyOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
    }

    // API cho ADMIN - Cập nhật status của bất kỳ đơn nào
    @PutMapping("/orders/admin/{orderId}/status")
    @ApiMessage("Order status updated successfully")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }

}
