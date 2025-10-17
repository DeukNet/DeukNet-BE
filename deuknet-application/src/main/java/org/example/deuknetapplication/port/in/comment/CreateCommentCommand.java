package org.example.deuknetapplication.port.in.comment;

import java.util.UUID;

public class CreateCommentCommand {
    private UUID postId;
    private String content;
    private UUID parentCommentId;

    protected CreateCommentCommand() {
    }

    public CreateCommentCommand(UUID postId, String content, UUID parentCommentId) {
        this.postId = postId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
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
