package org.example.deuknetapplication.port.in.post;

import java.util.List;
import java.util.UUID;

public class CreatePostApplicationRequest {
    private String title;
    private String content;
    private List<UUID> categoryIds;

    protected CreatePostApplicationRequest() {
    }

    public CreatePostApplicationRequest(String title, String content, List<UUID> categoryIds) {
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
