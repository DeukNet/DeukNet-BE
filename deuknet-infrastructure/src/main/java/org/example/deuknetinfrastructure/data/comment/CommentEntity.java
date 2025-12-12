package org.example.deuknetinfrastructure.data.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.deuknetdomain.domain.post.AuthorType;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "comments")
public class CommentEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "post_id", nullable = false, columnDefinition = "UUID")
    private UUID postId;

    @Column(name = "author_id", nullable = false, columnDefinition = "UUID")
    private UUID authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "parent_comment_id", columnDefinition = "UUID")
    private UUID parentCommentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 20)
    private AuthorType authorType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CommentEntity() {
    }

    public CommentEntity(UUID id, UUID postId, UUID authorId, String content,
                        UUID parentCommentId, AuthorType authorType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.authorType = authorType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
