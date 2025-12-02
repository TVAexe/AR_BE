package com.example.AR_BE.domain;

import java.time.Instant;
import java.util.List;

import com.example.AR_BE.utils.SecurityUtils;
import com.example.AR_BE.utils.constants.StatusEnum;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    // Quan hệ với OrderItem
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    private Double totalAmount;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ===== Audit =====
    @PrePersist
    public void handleCreate() {
        this.createdBy = SecurityUtils.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleUpdate() {
        this.updatedBy = SecurityUtils.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }

    // ===== Getter / Setter cho Status =====
    public StatusEnum getStatus() {
        return this.status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }
}
