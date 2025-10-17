package org.example.deuknetapplication.port.in.post;

import java.util.List;
import java.util.UUID;

public class UpdatePostCommand {
    private UUID postId;
    private String title;
    private String content;
    private List<UUID> categoryIds;

    protected UpdatePostCommand() {
    }

    public UpdatePostCommand(UUID postId, String title, String content, List<UUID> categoryIds) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.categoryIds = categoryIds;
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

    public List<UUID> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<UUID> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
