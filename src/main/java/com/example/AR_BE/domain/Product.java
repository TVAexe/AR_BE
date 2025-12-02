package com.example.AR_BE.domain;

import java.time.Instant;
import java.util.List;

import com.example.AR_BE.utils.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double oldPrice;
    private Double saleRate;
    private Integer quantity;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"products", "hibernateLazyInitializer", "handler"})
    private Category category;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> items;

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
