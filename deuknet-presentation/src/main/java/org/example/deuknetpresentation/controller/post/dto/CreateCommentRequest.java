package org.example.deuknetpresentation.controller.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "댓글 작성 요청")
public class CreateCommentRequest {
    
    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!", required = true)
    private String content;
    
    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID parentCommentId;

    public CreateCommentRequest() {
    }

    public CreateCommentRequest(String content, UUID parentCommentId) {
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}
