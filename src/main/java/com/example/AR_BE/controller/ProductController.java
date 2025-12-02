package com.example.AR_BE.controller;

import com.example.AR_BE.domain.Product;
import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.domain.request.CreateProductDTO;
import com.example.AR_BE.domain.request.UpdateProductDTO;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.service.ProductService;
import com.example.AR_BE.utils.exception.IdInvalidException;
import com.example.AR_BE.utils.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.AR_BE.repository.ProductRepository;

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
    public ResultPaginationDTO getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {

        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = (root, query, cb) -> {
            if (categoryId != null) {
                return cb.equal(root.get("category").get("id"), categoryId);
            }
            return cb.conjunction();
        };

        Page<Product> pageProduct = productRepository.findAll(spec, pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page + 1);
        meta.setPageSize(size);
        meta.setPages(pageProduct.getTotalPages());
        meta.setTotal(pageProduct.getTotalElements());

        result.setMeta(meta);
        result.setResult(pageProduct.getContent());

        return result;
    }

}
