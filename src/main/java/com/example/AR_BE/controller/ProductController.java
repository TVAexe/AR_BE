package com.example.AR_BE.controller;

import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.domain.request.CreateProductDTO;
import com.example.AR_BE.domain.request.UpdateProductDTO;
import com.example.AR_BE.service.ProductService;
import com.example.AR_BE.utils.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    @ApiMessage("Product created successfully")
    public ResponseEntity<ProductDTO> create(
            @Valid @RequestBody CreateProductDTO req) {
        return ResponseEntity.ok(productService.create(req));
    }

    @PutMapping("/{id}")
    @ApiMessage("Product updated successfully")
    public ResponseEntity<ProductDTO> update(
            @PathVariable Long id,
            @RequestBody UpdateProductDTO req) {
        return ResponseEntity.ok(productService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Product deleted successfully")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok().build();
    }
}
