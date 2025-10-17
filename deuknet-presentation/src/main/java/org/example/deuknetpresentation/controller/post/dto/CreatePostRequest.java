package org.example.deuknetpresentation.controller.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.deuknetapplication.port.in.post.CreatePostApplicationRequest;

import java.util.List;
import java.util.UUID;

@Schema(description = "게시글 작성 요청")
public class CreatePostRequest extends CreatePostApplicationRequest {

    public CreatePostRequest() {
        super();
    }

    public CreatePostRequest(String title, String content, List<UUID> categoryIds) {
        super(title, content, categoryIds);
    }

    @Override
    @Schema(description = "게시글 제목", example = "Spring Boot로 REST API 만들기", required = true)
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    @Schema(description = "게시글 내용", example = "이번 포스트에서는 Spring Boot를 사용하여...", required = true)
    public String getContent() {
        return super.getContent();
    }

    @Override
    @Schema(description = "카테고리 ID 목록", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    public List<UUID> getCategoryIds() {
        return super.getCategoryIds();
    }
}
