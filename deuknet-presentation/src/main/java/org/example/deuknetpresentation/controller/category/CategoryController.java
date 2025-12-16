package org.example.deuknetpresentation.controller.category;

import org.example.deuknetapplication.port.in.category.CategoryRankingResponse;
import org.example.deuknetapplication.port.in.category.CategoryResponse;
import org.example.deuknetapplication.port.in.category.CreateCategoryUseCase;
import org.example.deuknetapplication.port.in.category.DeleteCategoryUseCase;
import org.example.deuknetapplication.port.in.category.GetAllCategoriesUseCase;
import org.example.deuknetapplication.port.in.category.GetCategoryRankingUseCase;
import org.example.deuknetapplication.port.in.category.GrantCategoryOwnershipUseCase;
import org.example.deuknetapplication.port.in.category.UpdateCategoryUseCase;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetpresentation.controller.category.dto.CreateCategoryRequest;
import org.example.deuknetpresentation.controller.category.dto.UpdateCategoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController implements CategoryApi {

    private final GetAllCategoriesUseCase getAllCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final GetCategoryRankingUseCase getCategoryRankingUseCase;
    private final GrantCategoryOwnershipUseCase grantCategoryOwnershipUseCase;

    public CategoryController(
            GetAllCategoriesUseCase getAllCategoriesUseCase,
            CreateCategoryUseCase createCategoryUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            DeleteCategoryUseCase deleteCategoryUseCase,
            GetCategoryRankingUseCase getCategoryRankingUseCase,
            GrantCategoryOwnershipUseCase grantCategoryOwnershipUseCase
    ) {
        this.getAllCategoriesUseCase = getAllCategoriesUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
        this.getCategoryRankingUseCase = getCategoryRankingUseCase;
        this.grantCategoryOwnershipUseCase = grantCategoryOwnershipUseCase;
    }

    @Override
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return getAllCategoriesUseCase.getAllCategories();
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
        updateCategoryUseCase.updateCategory(categoryId, request);
    }

    @Override
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID categoryId) {
        deleteCategoryUseCase.deleteCategory(categoryId);
    }

    @GetMapping("/ranking")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CategoryRankingResponse>> getCategoryRanking(
            @RequestParam(defaultValue = "10") int size
    ) {
        // 최대 100개로 제한
        if (size > 100) {
            size = 100;
        }

        List<CategoryRankingResponse> rankings = getCategoryRankingUseCase.getCategoryRanking(size);
        return ResponseEntity.ok(rankings);
    }

    @PutMapping("/{categoryId}/owner/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grantOwnership(
            @PathVariable UUID categoryId,
            @PathVariable UUID targetUserId
    ) {
        grantCategoryOwnershipUseCase.grantOwnership(categoryId, targetUserId);
    }
}
