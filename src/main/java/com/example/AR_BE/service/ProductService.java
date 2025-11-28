package com.example.AR_BE.service;

import com.example.AR_BE.domain.Category;
import com.example.AR_BE.domain.Product;
import com.example.AR_BE.domain.dto.CategoryDTO;
import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.domain.request.CreateProductDTO;
import com.example.AR_BE.domain.request.UpdateProductDTO;
import com.example.AR_BE.repository.ProductRepository;
import com.example.AR_BE.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Get all products
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get by id
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return null;
        return convertToDTO(product);
    }

    // Create product
    public ProductDTO createProduct(Product product) {
        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    // Update product
    public ProductDTO updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Product not found with id " + id));

        product.setName(productDetails.getName());
        product.setOldPrice(productDetails.getOldPrice());
        product.setSaleRate(productDetails.getSaleRate());
        product.setQuantity(productDetails.getQuantity());
        product.setDescription(productDetails.getDescription());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory());

        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    // Delete product
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Convert Product entity to ProductDTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setOldPrice(product.getOldPrice());
        dto.setSaleRate(product.getSaleRate());
        dto.setQuantity(product.getQuantity());
        dto.setDescription(product.getDescription());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());
        dto.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) {
            dto.setCategory(new CategoryDTO(
                    product.getCategory().getId(),
                    product.getCategory().getName()
            ));
        }

        return dto;
    }

}
