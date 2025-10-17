package org.example.deuknetapplication.port.in.comment;

import java.util.UUID;

public class UpdateCommentApplicationRequest {
    private UUID commentId;
    private String content;

    protected UpdateCommentApplicationRequest() {
    }

    public UpdateCommentApplicationRequest(UUID commentId, String content) {
        this.commentId = commentId;
        this.content = content;
    }

    public UUID getCommentId() {
        return commentId;
    }

    public void setCommentId(UUID commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
