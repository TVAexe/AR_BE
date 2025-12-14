package com.example.AR_BE.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_ar_model")
public class ProductARModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String glbUrl;

    private Float scaleX = 1.0f;
    private Float scaleY = 1.0f;
    private Float scaleZ = 1.0f;

    private Float rotationY = 0f;

    private Boolean isArEnabled = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;
}
