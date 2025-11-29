package com.example.AR_BE.service;

import com.example.AR_BE.domain.Category;
import com.example.AR_BE.domain.Product;
import com.example.AR_BE.domain.dto.CategoryDTO;
import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.domain.request.CreateProductDTO;
import com.example.AR_BE.domain.request.UpdateProductDTO;
import com.example.AR_BE.repository.CategoryRepository;
import com.example.AR_BE.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    public boolean existsById(Long id) {
        return productRepo.existsById(id);
    }

    // GET all
    public List<ProductDTO> getAll() {
        return productRepo.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // GET by ID
    public ProductDTO getById(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDTO(p);
    }

    // CREATE
    public ProductDTO create(CreateProductDTO req) {
        Category category = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product p = new Product();
        p.setName(req.getName());
        p.setOldPrice(req.getOldPrice());
        p.setSaleRate(req.getSaleRate());
        p.setQuantity(req.getQuantity());
        p.setDescription(req.getDescription());
        p.setImageUrl(req.getImageUrl());
        p.setCategory(category);

        productRepo.save(p);
        return toDTO(p);
    }

    // UPDATE
    public ProductDTO update(Long id, UpdateProductDTO req) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (req.getName() != null) p.setName(req.getName());
        if (req.getOldPrice() != null) p.setOldPrice(req.getOldPrice());
        if (req.getSaleRate() != null) p.setSaleRate(req.getSaleRate());
        if (req.getQuantity() != null) p.setQuantity(req.getQuantity());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getImageUrl() != null) p.setImageUrl(req.getImageUrl());

        if (req.getCategoryId() != null) {
            Category category = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            p.setCategory(category);
        }

        productRepo.save(p);
        return toDTO(p);
    }

    // DELETE
    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    // Convert Entity -> DTO
    private ProductDTO toDTO(Product p) {
        CategoryDTO categoryDTO = new CategoryDTO(
                p.getCategory().getId(),
                p.getCategory().getName()
        );

        return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getOldPrice(),
                p.getSaleRate(),
                p.getQuantity(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getCreatedBy(),
                p.getUpdatedBy(),
                p.getImageUrl(),
                categoryDTO
        );
    }
}
