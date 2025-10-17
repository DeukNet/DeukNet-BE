package org.example.deuknetpresentation.controller.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.deuknetapplication.port.in.category.CreateCategoryApplicationRequest;

import java.util.UUID;

@Schema(description = "카테고리 생성 요청")
public class CreateCategoryRequest extends CreateCategoryApplicationRequest {

    public CreateCategoryRequest() {
        super();
    }

    public CreateCategoryRequest(String name, UUID parentCategoryId) {
        super(name, parentCategoryId);
    }

    @Override
    @Schema(description = "카테고리 이름", example = "기술", required = true)
    public String getName() {
        return super.getName();
    }

    @Override
    @Schema(description = "부모 카테고리 ID (최상위 카테고리인 경우 null)", example = "123e4567-e89b-12d3-a456-426614174000")
    public UUID getParentCategoryId() {
        return super.getParentCategoryId();
    }
}
