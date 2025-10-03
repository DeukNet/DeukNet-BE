package org.example.deuknetdomain.model.command.post.post;

import java.time.LocalDateTime;
import java.util.UUID;

public class Post {

    private final UUID id;
    private String title;
    private String content;
    private final UUID authorId;
    private PostStatus status;
    private Long viewCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Post(UUID id, String title, String content, UUID authorId,
                 PostStatus status, Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.status = status;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post create(String title, String content, UUID authorId) {
        validateTitle(title);
        validateContent(content);
        return new Post(
                UUID.randomUUID(),
                title,
                content,
                authorId,
                PostStatus.DRAFT,
                0L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static Post restore(UUID id, String title, String content, UUID authorId,
                               PostStatus status, Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Post(id, title, content, authorId, status, viewCount, createdAt, updatedAt);
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Post title cannot be empty");
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }
    }

    public void updateContent(String title, String content) {
        validateTitle(title);
        validateContent(content);
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void publish() {
        if (status != PostStatus.DRAFT) {
            throw new IllegalStateException("Only draft posts can be published");
        }
        this.status = PostStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = PostStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.status = PostStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public PostStatus getStatus() {
        return status;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
