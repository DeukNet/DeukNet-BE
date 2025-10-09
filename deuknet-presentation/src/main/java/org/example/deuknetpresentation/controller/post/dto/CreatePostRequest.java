package org.example.deuknetpresentation.controller.post.dto;

import java.util.List;
import java.util.UUID;

public class CreatePostRequest {
    private String title;
    private String content;
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
