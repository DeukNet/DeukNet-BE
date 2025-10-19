package org.example.deuknetdomain.model.command.post;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.AggregateRoot;
import org.example.deuknetdomain.common.vo.Content;
import org.example.deuknetdomain.common.vo.Title;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Post extends AggregateRoot {

    private Title title;
    private Content content;
    private final UUID authorId;
    private PostStatus status;
    private Long viewCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Post(UUID id, Title title, Content content, UUID authorId,
                 PostStatus status, Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.status = status;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post create(Title title, Content content, UUID authorId) {
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

    public static Post restore(UUID id, Title title, Content content, UUID authorId,
                               PostStatus status, Long viewCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Post(id, title, content, authorId, status, viewCount, createdAt, updatedAt);
    }

    public void updateContent(Title title, Content content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void publish() {
        if (status != PostStatus.DRAFT) {
            throw new org.example.deuknetdomain.model.command.post.exception.CannotPublishNonDraftPostException();
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

}
