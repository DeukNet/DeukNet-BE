package org.example.deuknetdomain.domain.comment;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.AggregateRoot;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.domain.post.AuthorType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
public class Comment extends AggregateRoot {

    private final UUID postId;
    private final UUID authorId;
    private Content content;
    private final UUID parentCommentId;
    private final AuthorType authorType;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Comment(UUID id, UUID postId, UUID authorId, Content content,
                   UUID parentCommentId, AuthorType authorType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.authorType = authorType != null ? authorType : AuthorType.REAL;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Comment create(UUID postId, UUID authorId, Content content, UUID parentCommentId, AuthorType authorType) {
        return new Comment(
                UuidCreator.getTimeOrderedEpoch(),
                postId,
                authorId,
                content,
                parentCommentId,
                authorType,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static Comment restore(UUID id, UUID postId, UUID authorId, Content content,
                                 UUID parentCommentId, AuthorType authorType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Comment(id, postId, authorId, content, parentCommentId, authorType, createdAt, updatedAt);
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
