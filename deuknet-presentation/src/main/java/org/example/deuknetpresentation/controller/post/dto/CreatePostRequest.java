package org.example.deuknetpresentation.controller.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "게시글 작성 요청")
public class CreatePostRequest {
    
    @Schema(description = "게시글 제목", example = "Spring Boot로 REST API 만들기", required = true)
    private String title;
    
    @Schema(description = "게시글 내용", example = "이번 포스트에서는 Spring Boot를 사용하여...", required = true)
    private String content;
    
    @Schema(description = "카테고리 ID 목록", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
    private List<UUID> categoryIds;

    public CreatePostRequest() {
    }

    public CreatePostRequest(String title, String content, List<UUID> categoryIds) {
        this.title = title;
        this.content = content;
        this.categoryIds = categoryIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<UUID> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<UUID> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
