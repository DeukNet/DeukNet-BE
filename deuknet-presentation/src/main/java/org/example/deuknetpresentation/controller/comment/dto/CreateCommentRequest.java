package org.example.deuknetpresentation.controller.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.deuknetapplication.port.in.comment.CreateCommentAppliationRequest;

import java.util.UUID;

@Schema(description = "댓글 작성 요청")
public class CreateCommentRequest extends CreateCommentAppliationRequest {

    public CreateCommentRequest() {
        super();
    }

    public CreateCommentRequest(UUID postId, String content, UUID parentCommentId) {
        super(postId, content, parentCommentId);
    }

    @Override
    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!", required = true)
    public String getContent() {
        return super.getContent();
    }

    @Override
    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "123e4567-e89b-12d3-a456-426614174000")
    public UUID getParentCommentId() {
        return super.getParentCommentId();
    }
}
