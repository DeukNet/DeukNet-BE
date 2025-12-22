package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

public class UpdatePostApplcationRequest {
    private UUID postId;
    private String title;
    private String content;
    private UUID categoryId;
    private String thumbnailImageUrl;

    protected UpdatePostApplcationRequest() {
    }

    public UpdatePostApplcationRequest(UUID postId, String title, String content, UUID categoryId, String thumbnailImageUrl) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.thumbnailImageUrl = thumbnailImageUrl;
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

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }
}
