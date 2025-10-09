package org.example.deuknetpresentation.controller.category;

import org.example.deuknetapplication.usecase.category.CreateCategoryUseCase;
import org.example.deuknetapplication.usecase.category.DeleteCategoryUseCase;
import org.example.deuknetapplication.usecase.category.UpdateCategoryUseCase;
import org.example.deuknetdomain.common.vo.CategoryName;
import org.example.deuknetpresentation.controller.category.dto.CreateCategoryRequest;
import org.example.deuknetpresentation.controller.category.dto.UpdateCategoryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    public CategoryController(
            CreateCategoryUseCase createCategoryUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            DeleteCategoryUseCase deleteCategoryUseCase
    ) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

    @PostMapping
    public ResponseEntity<UUID> createCategory(@RequestBody CreateCategoryRequest request) {
        CreateCategoryUseCase.CreateCategoryCommand command = new CreateCategoryUseCase.CreateCategoryCommand(
                CategoryName.of(request.getName()),
                request.getParentCategoryId()
        );
        
        UUID categoryId = createCategoryUseCase.createCategory(command);
        return ResponseEntity.ok(categoryId);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable UUID categoryId,
            @RequestBody UpdateCategoryRequest request
    ) {
        UpdateCategoryUseCase.UpdateCategoryCommand command = new UpdateCategoryUseCase.UpdateCategoryCommand(
                categoryId,
                CategoryName.of(request.getName())
        );
        
        updateCategoryUseCase.updateCategory(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        deleteCategoryUseCase.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }
}
