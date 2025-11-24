package com.example.AR_BE.domain;

import java.time.Instant;
import java.util.List;

import com.example.AR_BE.utils.SecurityUtils;
import com.example.AR_BE.utils.constants.PaymentMethodEnum;
import com.example.AR_BE.utils.constants.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
    @NotNull(message = "User is required")
    @JsonIgnoreProperties(value = { "orders" })
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "order_products",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id"))
    @JsonIgnoreProperties(value = { "orders", "categories" })
    private List<Product> items;

    private Double totalAmount;
    private StatusEnum status;
    private PaymentMethodEnum paymentMethod;
    private String shippingAddress;

    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleCreate() {
        this.createdBy = SecurityUtils.getCurrentUserLogin().isPresent() 
                ? SecurityUtils.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleUpdate() {
        this.updatedBy = SecurityUtils.getCurrentUserLogin().isPresent() 
                ? SecurityUtils.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }
}