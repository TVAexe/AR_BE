package com.example.AR_BE.service;

import com.example.AR_BE.domain.Category;
import com.example.AR_BE.domain.Product;
import com.example.AR_BE.domain.ProductARModel;
import com.example.AR_BE.domain.dto.CategoryDTO;
import com.example.AR_BE.domain.dto.ProductDTO;
import com.example.AR_BE.domain.dto.ProductARModelDTO;
import com.example.AR_BE.domain.request.CreateProductDTO;
import com.example.AR_BE.domain.request.UpdateProductDTO;
import com.example.AR_BE.repository.CategoryRepository;
import com.example.AR_BE.repository.ProductRepository;

import jakarta.transaction.Transactional;
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

        if (req.getModelFile() != null) {
            MultipartFile file = req.getModelFile();
            try {
                String fileName = fileService.uploadFile(file);
                String modelUrl = fileService.getFileUrl(fileName);

                ProductARModel arModel = new ProductARModel();
                arModel.setGlbUrl(modelUrl);
                arModel.setProduct(p);
                p.setArModel(arModel);

            } catch (Exception e) {
                throw new RuntimeException("Fail to upload 3D model" + e.getMessage());
            }
        }

        p.setCategory(category);

        productRepo.save(p);
        return toDTO(p);
    }

    // UPDATE
    public ProductDTO update(Long id, UpdateProductDTO req) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (req.getName() != null)
            p.setName(req.getName());
        if (req.getOldPrice() != null)
            p.setOldPrice(req.getOldPrice());
        if (req.getSaleRate() != null)
            p.setSaleRate(req.getSaleRate());
        if (req.getQuantity() != null)
            p.setQuantity(req.getQuantity());
        if (req.getDescription() != null)
            p.setDescription(req.getDescription());
        if (req.getImageUrl() != null && !req.getImageUrl().isEmpty()) {
            p.setImageUrl(req.getImageUrl());
        }

        if (req.getCategoryId() != null) {
            Category category = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            p.setCategory(category);
        }

        if (req.getModelUrl() != null && !req.getModelUrl().isBlank()) {
            ProductARModel arModel = p.getArModel();

            if (arModel == null) {
                arModel = new ProductARModel();
                arModel.setProduct(p);
                p.setArModel(arModel);
            }

            arModel.setGlbUrl(req.getModelUrl());
        }

        productRepo.save(p);
        return toDTO(p);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<String> imageUrls = p.getImageUrl();
        // / Xóa product khỏi DB trước
        productRepo.deleteById(id);
        // Sau đó xóa ảnh trên S3 (ngoài transaction)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                try {
                    fileService.deleteFileByUrl(imageUrl);
                } catch (Exception e) {
                    System.err.println("Failed to delete image from S3: " + imageUrl + " - " + e.getMessage());
                }
            }
        }

        ProductARModel arModel = p.getArModel();
        if (arModel != null && arModel.getGlbUrl() != null) {
            String modelUrl = arModel.getGlbUrl();
            if (!modelUrl.isEmpty()) {
                try {
                    fileService.deleteFile(modelUrl);
                } catch (Exception e) {
                    System.err.println("Failed to delete model from S3: " + modelUrl + " - " + e.getMessage());
                }
            }
        }
    }

    // Convert Entity -> DTO
    private ProductDTO toDTO(Product p) {
        CategoryDTO categoryDTO = new CategoryDTO(
                p.getCategory().getId(),
                p.getCategory().getName());

        ProductARModelDTO arDto = null;
        if (p.getArModel() != null) {
            ProductARModel m = p.getArModel();
            arDto = new ProductARModelDTO(
                    m.getId(),
                    m.getGlbUrl(),
                    m.getScaleX(),
                    m.getScaleY(),
                    m.getScaleZ(),
                    m.getRotationY(),
                    m.getIsArEnabled()
            );
        }

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
                arDto,
                categoryDTO);
    }

    public Product getAndValidateProduct(Long productId, int qty) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (p.getQuantity() < qty) {
            throw new RuntimeException("Not enough stock for product " + productId);
        }

        return p;
    }

    public void deductStock(Product p, int quantity) {
        p.setQuantity(p.getQuantity() - quantity);
        productRepo.save(p);
    }

    @Transactional
    public void increaseStock(Product product, int quantity) {
        product.setQuantity(product.getQuantity() + quantity);
        productRepo.save(product);
    }

    public ResultPaginationDTO getProductsWithCategory(int page, int size, Long categoryId) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Product> spec = (root, query, cb) -> {
            if (categoryId != null) {
                return cb.equal(root.get("category").get("id"), categoryId);
            }
            return cb.conjunction();
        };

        Page<Product> pageProduct = productRepo.findAll(spec, pageRequest);

        List<ProductDTO> productDTOs = pageProduct.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page + 1);
        meta.setPageSize(size);
        meta.setPages(pageProduct.getTotalPages());
        meta.setTotal(pageProduct.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(productDTOs);

        return result;
    }
}
