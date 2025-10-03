package org.example.deuknetdomain.model.command.comment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class Comment {

    private final UUID id;
    private final UUID postId;
    private final UUID authorId;
    private String content;
    private final UUID parentCommentId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Comment(UUID id, UUID postId, UUID authorId, String content,
                   UUID parentCommentId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Comment create(UUID postId, UUID authorId, String content, UUID parentCommentId) {
        validateContent(content);
        return new Comment(
                UUID.randomUUID(),
                postId,
                authorId,
                content,
                parentCommentId,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static Comment restore(UUID id, UUID postId, UUID authorId, String content,
                                 UUID parentCommentId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Comment(id, postId, authorId, content, parentCommentId, createdAt, updatedAt);
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
    }

    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Optional<UUID> getParentCommentId() {
        return Optional.ofNullable(parentCommentId);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
