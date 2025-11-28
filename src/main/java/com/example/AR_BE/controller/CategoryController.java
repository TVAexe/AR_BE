package com.example.AR_BE.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.AR_BE.service.CategoryService;
import com.example.AR_BE.utils.exception.IdInvalidException;

import jakarta.validation.Valid;

import com.example.AR_BE.domain.Category;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createNewCategory(@Valid @RequestBody Category requestCategory)
            throws IdInvalidException {
        boolean isCategoryNameExisted = this.categoryService.checkNameExist(requestCategory.getName());
        if (isCategoryNameExisted) {
            throw new IdInvalidException("Loai " + requestCategory.getName() + " da ton tai");
        }

        Category newCategory = this.categoryService.handleCreateCategory(requestCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) throws IdInvalidException {
        boolean isCategoryExist = this.categoryService.checkIdExist(id);

        if (!isCategoryExist) {
            throw new IdInvalidException("Khong tim thay loai san pham voi id: " + id);
        }

        this.categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@Valid @PathVariable Long id,
            @RequestBody Category requets_category) throws IdInvalidException {

        if (requets_category.getName() == null || requets_category.getName().isEmpty()) {
            throw new IdInvalidException("Ten loai san pham khong duoc de trong");
        }

        boolean isCategoryExist = this.categoryService.checkIdExist(id);

        if (!isCategoryExist) {
            throw new IdInvalidException("Khong tim thay loai san pham voi id: " + id);
        }

        Category category = this.categoryService.getCategoryById(id);
        category.setName(requets_category.getName());
        category = this.categoryService.handleCreateCategory(category);

        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

}
