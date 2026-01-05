package org.example.deuknetpresentation.controller.category;

import org.example.deuknetapplication.port.in.category.CategoryRankingResponse;
import org.example.deuknetapplication.port.in.category.CategoryResponse;
import org.example.deuknetapplication.port.in.category.CreateCategoryUseCase;
import org.example.deuknetapplication.port.in.category.DeleteCategoryUseCase;
import org.example.deuknetapplication.port.in.category.GetAllCategoriesUseCase;
import org.example.deuknetapplication.port.in.category.GetCategoriesUseCase;
import org.example.deuknetapplication.port.in.category.GetCategoryRankingUseCase;
import org.example.deuknetapplication.port.in.category.GrantCategoryOwnershipUseCase;
import org.example.deuknetapplication.port.in.category.SearchCategoriesUseCase;
import org.example.deuknetapplication.port.in.category.UpdateCategoryUseCase;
import org.example.deuknetapplication.port.in.post.PageResponse;
import org.example.deuknetpresentation.controller.category.dto.CreateCategoryRequest;
import org.example.deuknetpresentation.controller.category.dto.UpdateCategoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController implements CategoryApi {

    private final GetAllCategoriesUseCase getAllCategoriesUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final GetCategoryRankingUseCase getCategoryRankingUseCase;
    private final GrantCategoryOwnershipUseCase grantCategoryOwnershipUseCase;
    private final SearchCategoriesUseCase searchCategoriesUseCase;

    public CategoryController(
            GetAllCategoriesUseCase getAllCategoriesUseCase,
            GetCategoriesUseCase getCategoriesUseCase,
            CreateCategoryUseCase createCategoryUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            DeleteCategoryUseCase deleteCategoryUseCase,
            GetCategoryRankingUseCase getCategoryRankingUseCase,
            GrantCategoryOwnershipUseCase grantCategoryOwnershipUseCase,
            SearchCategoriesUseCase searchCategoriesUseCase
    ) {
        this.getAllCategoriesUseCase = getAllCategoriesUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
        this.getCategoryRankingUseCase = getCategoryRankingUseCase;
        this.grantCategoryOwnershipUseCase = grantCategoryOwnershipUseCase;
        this.searchCategoriesUseCase = searchCategoriesUseCase;
    }

    @Override
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return getAllCategoriesUseCase.getAllCategories();
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<CategoryResponse>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 최대 100개로 제한
        if (size > 100) {
            size = 100;
        }

        PageResponse<CategoryResponse> response = getCategoriesUseCase.getCategories(page, size);
        return ResponseEntity.ok(response);
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

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<CategoryResponse>> searchCategories(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 최대 100개로 제한
        if (size > 100) {
            size = 100;
        }

        PageResponse<CategoryResponse> response = searchCategoriesUseCase.searchCategories(keyword, page, size);
        return ResponseEntity.ok(response);
    }
}
