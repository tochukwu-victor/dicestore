
package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.dto.CategoryDto;
import com.victoruk.dicestore.dto.CategoryRequestDto;
import com.victoruk.dicestore.entity.Category;
import com.victoruk.dicestore.exception.ResourceNotFoundException;
import com.victoruk.dicestore.exception.ResourceNotFoundExceptionn;
import com.victoruk.dicestore.repository.CategoryRepository;
import com.victoruk.dicestore.service.CategoryService;
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






//package com.victoruk.dicestore.service.impl;
//
//import com.victoruk.dicestore.dto.CategoryDto;
//import com.victoruk.dicestore.dto.CategoryRequestDto;
//import com.victoruk.dicestore.entity.Category;
//import com.victoruk.dicestore.exception.ResourceNotFoundException;
//import com.victoruk.dicestore.exception.ResourceNotFoundExceptionn;
//import com.victoruk.dicestore.mapper.CategoryMapper;
//import com.victoruk.dicestore.repository.CategoryRepository;
//import com.victoruk.dicestore.service.CategoryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class CategoryServiceImpl implements CategoryService {
//
//    private final CategoryRepository categoryRepository;
//    private final CategoryMapper categoryMapper;
//
//    @Override
//    public CategoryDto createCategory(CategoryRequestDto dto) {
//        if (categoryRepository.existsByName(dto.getName())) {
//            throw new IllegalArgumentException("Category with this name already exists");
//        }
//        Category category = categoryMapper.toEntity(dto);
//        return categoryMapper.toDto(categoryRepository.save(category));
//    }
//
//    @Override
//    public CategoryDto updateCategory(Long id, CategoryRequestDto dto) {
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundExceptionn("Category not found"));
//
//        categoryMapper.updateEntityFromDto(dto, category);
//        return categoryMapper.toDto(categoryRepository.save(category));
//    }
//
//    @Override
//    public void deleteCategory(Long id) {
//        if (!categoryRepository.existsById(id)) {
//            throw new ResourceNotFoundExceptionn("Category not found");
//        }
//        categoryRepository.deleteById(id);
//    }
//
//    @Override
//    public CategoryDto getCategoryById(Long id) {
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundExceptionn("Category not found"));
//        return categoryMapper.toDto(category);
//    }
//
//    @Override
//    public List<CategoryDto> getAllCategories() {
//        return categoryRepository.findAll()
//                .stream()
//                .map(categoryMapper::toDto)
//                .toList();
//    }
//}
