package org.example.deuknetdomain.model.command.comment;

import lombok.Getter;
import org.example.deuknetdomain.common.vo.Content;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Comment {
    private final UUID id;
    private final UUID postId;
    private final UUID authorId;
    private Content content;
    private final UUID parentCommentId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Comment(UUID id, UUID postId, UUID authorId, Content content,
                   UUID parentCommentId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Comment create(UUID postId, UUID authorId, Content content, UUID parentCommentId) {
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

    public static Comment restore(UUID id, UUID postId, UUID authorId, Content content,
                                 UUID parentCommentId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Comment(id, postId, authorId, content, parentCommentId, createdAt, updatedAt);
    }

    public void updateContent(Content content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public Optional<UUID> getParentCommentId() {
        return Optional.ofNullable(parentCommentId);
    }
}
