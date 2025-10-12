package org.example.deuknetpresentation.controller.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "카테고리 생성 요청")
public class CreateCategoryRequest {
    
    @Schema(description = "카테고리 이름", example = "기술", required = true)
    private String name;
    
    @Schema(description = "부모 카테고리 ID (최상위 카테고리인 경우 null)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID parentCategoryId;

    public CreateCategoryRequest() {
    }

    public CreateCategoryRequest(String name, UUID parentCategoryId) {
        this.name = name;
        this.parentCategoryId = parentCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(UUID parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
}
