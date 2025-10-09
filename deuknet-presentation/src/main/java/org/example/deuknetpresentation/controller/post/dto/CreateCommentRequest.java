package org.example.deuknetpresentation.controller.post.dto;

import java.util.UUID;

public class CreateCommentRequest {
    private String content;
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
