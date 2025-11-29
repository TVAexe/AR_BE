package com.example.AR_BE.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.AR_BE.domain.Order;
import com.example.AR_BE.domain.User;
import com.example.AR_BE.utils.constants.StatusEnum;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserAndStatus(User user, StatusEnum status);

    List<Order> findByUser(User user);

    Page<Order> findByUserIdAndStatus(Long userId, StatusEnum status, Pageable pageable);

    Page<Order> findByStatus(StatusEnum status, Pageable pageable);
}
