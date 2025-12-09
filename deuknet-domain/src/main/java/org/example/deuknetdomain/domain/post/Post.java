package org.example.deuknetdomain.domain.post;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.AggregateRoot;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import org.example.deuknetdomain.domain.post.exception.CannotPublishNonDraftPostException;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Post extends AggregateRoot {

    private Title title;
    private Content content;
    private final UUID authorId;
    private UUID categoryId;
    private PostStatus status;
    private final AuthorType authorType;
    // viewCount 제거 - Reaction 테이블에서 집계
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Post(UUID id, Title title, Content content, UUID authorId, UUID categoryId,
                 PostStatus status, AuthorType authorType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.status = status;
        this.authorType = authorType != null ? authorType : AuthorType.REAL;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post create(Title title, Content content, UUID authorId, UUID categoryId, AuthorType authorType) {
        return new Post(
                UUID.randomUUID(),
                title,
                content,
                authorId,
                categoryId,
                PostStatus.DRAFT,
                authorType,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static Post restore(UUID id, Title title, Content content, UUID authorId, UUID categoryId,
                               PostStatus status, AuthorType authorType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Post(id, title, content, authorId, categoryId, status, authorType, createdAt, updatedAt);
    }

    public void updateContent(Title title, Content content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategory(UUID categoryId) {
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
    }

    public void publish() {
        if (status != PostStatus.DRAFT) {
            throw new CannotPublishNonDraftPostException();
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

    // incrementViewCount() 제거 - Reaction으로 처리

}
