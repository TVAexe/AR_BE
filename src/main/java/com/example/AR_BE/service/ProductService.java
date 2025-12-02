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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import com.example.AR_BE.service.FileService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final FileService fileService;

    public boolean existsById(Long id) {
        return productRepo.existsById(id);
    }

    // GET all
    public List<ProductDTO> getAll() {
        return productRepo.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ResultPaginationDTO getProducts(int page, int pageSize, String search, Long categoryId) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());

        Specification<Product> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (search != null && !search.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category").get("id"), categoryId));
            }
            return predicate;
        };

        Page<Product> productPage = productRepo.findAll(spec, pageRequest);

        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page);
        meta.setPageSize(pageSize);
        meta.setPages(productPage.getTotalPages());
        meta.setTotal(productPage.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(productDTOs);

        return result;
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

        List<String> urls = new ArrayList<>();

        if (req.getImages() != null) {
            for (MultipartFile file : req.getImages()) {
                try {
                    String fileName = fileService.uploadFile(file);
                    String fileUrl = fileService.getFileUrl(fileName);
                    urls.add(fileUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                }
            }
        }
        p.setImageUrl(urls);
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
        if (req.getImageUrl() != null && !req.getImageUrl().isEmpty()) {
            p.setImageUrl(req.getImageUrl());
        }
        
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
                p.getCategory().getName());

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
                categoryDTO);
    }
}
