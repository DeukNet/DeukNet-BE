package org.example.deuknetinfrastructure.data.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.deuknetdomain.domain.post.PostStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "posts")
public class PostEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "author_id", nullable = false, columnDefinition = "UUID")
    private UUID authorId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;  // 기본값 0 (Reaction 테이블에서 집계)

    public PostEntity() {
    }

    public PostEntity(UUID id, String title, String content, UUID authorId,
                      PostStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.viewCount = 0L;  // 기본값
    }

}
