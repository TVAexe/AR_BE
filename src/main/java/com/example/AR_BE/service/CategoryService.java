package com.example.AR_BE.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.AR_BE.domain.Category;
import com.example.AR_BE.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category handleCreateCategory(Category category) {
        return categoryRepository.save(category);
    }

    public boolean checkNameExist(String name) {
        return categoryRepository.existsByName(name);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public boolean checkIdExist(Long id) {
        return categoryRepository.existsById(id);
    }

    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
