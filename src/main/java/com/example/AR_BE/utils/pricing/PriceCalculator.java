package com.example.AR_BE.utils.pricing;

import org.springframework.stereotype.Component;

import com.example.AR_BE.domain.Product;

@Component
public class PriceCalculator {
    public double calculatePrice(Product product) {
        double basePrice = product.getOldPrice() != null ? product.getOldPrice() : 0.0;
        double saleRate = product.getSaleRate() != null ? product.getSaleRate() : 0.0;
        double discount = (saleRate >= 1.0) ? saleRate / 100.0 : saleRate;

        return basePrice * (1 - discount);
    }
}
