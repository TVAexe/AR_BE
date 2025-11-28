package com.example.AR_BE.controller;

import com.example.AR_BE.domain.Product;
import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.service.ProductService;
import com.example.AR_BE.utils.exception.IdInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) throws IdInvalidException {
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            throw new IdInvalidException("Product with id " + id + " does not exist");
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody Product product) {
        ProductDTO createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody Product product) throws IdInvalidException {
        ProductDTO existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            throw new IdInvalidException("Product with id " + id + " does not exist");
        }
        ProductDTO updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) throws IdInvalidException {
        ProductDTO existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            throw new IdInvalidException("Product with id " + id + " does not exist");
        }
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

}
