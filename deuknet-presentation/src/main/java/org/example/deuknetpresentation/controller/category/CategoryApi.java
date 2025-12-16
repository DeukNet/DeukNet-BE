package org.example.deuknetpresentation.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.deuknetapplication.port.in.category.CategoryResponse;
import org.example.deuknetpresentation.controller.category.dto.CreateCategoryRequest;
import org.example.deuknetpresentation.controller.category.dto.UpdateCategoryRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Category", description = "카테고리 관리 API")
public interface CategoryApi {

    @Operation(
            summary = "전체 카테고리 조회",
            description = "모든 루트 카테고리를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            )
    })
    List<CategoryResponse> getAllCategories();

    @Operation(
            summary = "카테고리 생성",
            description = "새로운 카테고리를 생성합니다. 부모 카테고리 ID를 지정하여 하위 카테고리를 만들 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "카테고리 생성 성공",
                    content = @Content(schema = @Schema(implementation = UUID.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복된 카테고리명 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    UUID createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "카테고리 생성 정보",
                    required = true
            )
            @RequestBody CreateCategoryRequest request
    );

    @Operation(
            summary = "카테고리 수정",
            description = "기존 카테고리의 설명과 썸네일 이미지를 수정합니다. ADMIN 또는 카테고리 소유자만 수정 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    void updateCategory(
            @Parameter(description = "카테고리 ID", required = true)
            @PathVariable UUID categoryId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "카테고리 수정 정보",
                    required = true
            )
            @RequestBody UpdateCategoryRequest request
    );

    @Operation(
            summary = "카테고리 삭제",
            description = "카테고리를 삭제합니다. 하위 카테고리가 있는 경우 삭제할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "하위 카테고리가 존재하여 삭제 불가"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    void deleteCategory(
            @Parameter(description = "카테고리 ID", required = true)
            @PathVariable UUID categoryId
    );
}
