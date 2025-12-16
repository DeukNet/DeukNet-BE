package org.example.deuknetpresentation.controller.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.deuknetapplication.port.in.category.CreateCategoryApplicationRequest;

import java.util.UUID;

@Schema(description = "카테고리 생성 요청")
public class CreateCategoryRequest extends CreateCategoryApplicationRequest {

    public CreateCategoryRequest() {
        super();
    }

    public CreateCategoryRequest(String name, UUID parentCategoryId, String description, String thumbnailImageUrl, UUID ownerId) {
        super(name, parentCategoryId, description, thumbnailImageUrl, ownerId);
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

    @Override
    @Schema(description = "카테고리 설명", example = "기술 관련 게시물")
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
    public String getThumbnailImageUrl() {
        return super.getThumbnailImageUrl();
    }

    @Override
    @Schema(description = "카테고리 소유자 ID (null이면 admin만 수정 가능)", example = "123e4567-e89b-12d3-a456-426614174000")
    public UUID getOwnerId() {
        return super.getOwnerId();
    }
}
