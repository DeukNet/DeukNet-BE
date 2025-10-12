package org.example.deuknetpresentation.controller.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 수정 요청")
public class UpdateCommentRequest {
    
    @Schema(description = "댓글 내용", example = "수정된 댓글 내용입니다.", required = true)
    private String content;

    public UpdateCommentRequest() {
    }

    public UpdateCommentRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
