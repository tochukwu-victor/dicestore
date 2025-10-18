package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.CategoryDto;
import com.victoruk.dicestore.dto.CategoryRequestDto;
import com.victoruk.dicestore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryRequestDto dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

        @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories()); // Public
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequestDto dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping
//    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryRequestDto dto) {
//        return ResponseEntity.ok(categoryService.createCategory(dto));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<CategoryDto> updateCategory(
//            @PathVariable Long id,
//            @RequestBody CategoryRequestDto dto) {
//        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
//        categoryService.deleteCategory(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
//        return ResponseEntity.ok(categoryService.getCategoryById(id));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<CategoryDto>> getAllCategories() {
//        return ResponseEntity.ok(categoryService.getAllCategories());
//    }
}
