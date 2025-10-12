package org.example.deuknetpresentation.controller.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리 수정 요청")
public class UpdateCategoryRequest {
    
    @Schema(description = "카테고리 이름", example = "기술 & IT", required = true)
    private String name;

    public UpdateCategoryRequest() {
    }

    public UpdateCategoryRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
