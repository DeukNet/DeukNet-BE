package org.example.deuknetpresentation.controller.category;

import org.example.deuknetapplication.port.in.category.CreateCategoryUseCase;
import org.example.deuknetapplication.port.in.category.DeleteCategoryUseCase;
import org.example.deuknetapplication.port.in.category.UpdateCategoryUseCase;
import org.example.deuknetpresentation.controller.category.dto.CreateCategoryRequest;
import org.example.deuknetpresentation.controller.category.dto.UpdateCategoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController implements CategoryApi {

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

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createCategory(@RequestBody CreateCategoryRequest request) {
        return createCategoryUseCase.createCategory(request);
    }

    @Override
    @PutMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategory(@PathVariable UUID categoryId, @RequestBody UpdateCategoryRequest request) {
        request.setCategoryId(categoryId);
        updateCategoryUseCase.updateCategory(request);
    }

    @Override
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID categoryId) {
        deleteCategoryUseCase.deleteCategory(categoryId);
    }
}
