package com.example.AR_BE.controller;

import com.example.AR_BE.domain.Product;
import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.domain.request.CreateProductDTO;
import com.example.AR_BE.domain.request.UpdateProductDTO;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.repository.ProductRepository;
import com.example.AR_BE.service.ProductService;
import com.example.AR_BE.utils.exception.IdInvalidException;
import com.example.AR_BE.utils.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.AR_BE.domain.response.ResultPaginationDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    // @GetMapping("/products")
    // public ResponseEntity<List<ProductDTO>> getAllProducts() {
    // List<ProductDTO> products = productService.getAll();
    // return ResponseEntity.ok(products);
    // }

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

    @GetMapping("/products")
    public ResultPaginationDTO handleGetAllProducts(Specification<Product> spec, Pageable pageable) {
        Page<Product> pageProduct = this.productRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageProduct.getTotalPages());
        meta.setTotal(pageProduct.getTotalElements());
        result.setMeta(meta);
        result.setResult(pageProduct.getContent());
        return result;
    }

}
