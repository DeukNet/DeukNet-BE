package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

public class UpdatePostApplcationRequest {
    private UUID postId;
    private String title;
    private String content;
    private UUID categoryId;

    protected UpdatePostApplcationRequest() {
    }

    public UpdatePostApplcationRequest(UUID postId, String title, String content, UUID categoryId) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
}
