package org.example.deuknetpresentation.controller.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.deuknetapplication.port.in.category.UpdateCategoryApplicationRequest;

@Schema(description = "카테고리 수정 요청")
public class UpdateCategoryRequest extends UpdateCategoryApplicationRequest {

    public UpdateCategoryRequest() {
        super();
    }

    public UpdateCategoryRequest(String description, String thumbnailImageUrl) {
        super(description, thumbnailImageUrl);
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
}
