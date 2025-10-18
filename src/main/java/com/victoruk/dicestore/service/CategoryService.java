package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.CategoryDto;
import com.victoruk.dicestore.dto.CategoryRequestDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryRequestDto dto);
    CategoryDto updateCategory(Long id, CategoryRequestDto dto);
    void deleteCategory(Long id);
    CategoryDto getCategoryById(Long id);
    List<CategoryDto> getAllCategories();
}
