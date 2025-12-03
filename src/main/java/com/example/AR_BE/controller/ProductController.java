package com.example.AR_BE.controller;

import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.domain.request.CreateProductDTO;
import com.example.AR_BE.domain.request.UpdateProductDTO;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.service.ProductService;
import com.example.AR_BE.utils.exception.IdInvalidException;
import com.example.AR_BE.utils.annotation.ApiMessage;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<ResultPaginationDTO> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId) {
        ResultPaginationDTO result = productService.getProducts(page, pageSize, search, categoryId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) throws IdInvalidException {
        boolean isProductExist = productService.existsById(id);
        if (!isProductExist)
            throw new IdInvalidException("Product with id " + id + " not found");
        ProductDTO product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/products")
    @ApiMessage("Product created successfully")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @ModelAttribute CreateProductDTO req) throws IdInvalidException {
        ProductDTO newProduct = productService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @PutMapping("products/{id}")
    @ApiMessage("Product updated successfully")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductDTO req) throws IdInvalidException {
        boolean isProductExist = productService.existsById(id);
        if (!isProductExist)
            throw new IdInvalidException("Product with id " + id + " not found");
        ProductDTO updatedProduct = productService.update(id, req);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/products/{id}")
    @ApiMessage("Product deleted successfully")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IdInvalidException {
        boolean isProductExist = productService.existsById(id);
        if (!isProductExist)
            throw new IdInvalidException("Product with id " + id + " not found");
        productService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/with-category")
    public ResponseEntity<ResultPaginationDTO> getProductsWithCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {

        ResultPaginationDTO result = productService.getProductsWithCategory(page, size, categoryId);
        return ResponseEntity.ok(result);
    }

}
