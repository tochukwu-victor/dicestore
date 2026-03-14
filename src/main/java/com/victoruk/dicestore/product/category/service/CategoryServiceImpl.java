
package com.victoruk.dicestore.product.category.service;

import com.victoruk.dicestore.product.category.dto.CategoryDto;
import com.victoruk.dicestore.product.category.dto.CategoryRequestDto;
import com.victoruk.dicestore.product.category.entity.Category;
import com.victoruk.dicestore.common.exception.ResourceNotFoundExceptionn;
import com.victoruk.dicestore.product.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // --------- Create ---------
    @Override
    public CategoryDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return toDto(savedCategory);
    }

    // --------- Update ---------
    @Override
    public CategoryDto updateCategory(Long id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionn("Category not found"));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        Category updated = categoryRepository.save(category);
        return toDto(updated);
    }

    // --------- Delete ---------
    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundExceptionn("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    // --------- Get One ---------
    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptionn("Category not found"));
        return toDto(category);
    }

    // --------- Get All ---------
    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // --------- Mapping Helpers ---------
    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}


