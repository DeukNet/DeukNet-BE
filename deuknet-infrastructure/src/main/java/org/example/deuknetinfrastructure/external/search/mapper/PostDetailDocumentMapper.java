package org.example.deuknetinfrastructure.external.search.mapper;

import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * PostDetailDocument와 PostDetailProjection 간의 변환을 담당하는 Mapper
 */
@Component
public class PostDetailDocumentMapper {

    /**
     * PostDetailDocument를 PostDetailProjection으로 변환
     * Document와 Projection은 동일한 필드 구조를 유지
     */
    public PostDetailProjection toProjection(PostDetailDocument document, String unused1, String unused2, String unused3) {
        if (document == null) {
            return null;
        }

        return PostDetailProjection.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .authorId(document.getAuthorId() != null ? UUID.fromString(document.getAuthorId()) : null)
                .authorType(document.getAuthorType())
                .status(document.getStatus())
                .thumbnailImageUrl(document.getThumbnailImageUrl())
                .viewCount(document.getViewCount())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .categoryId(document.getCategoryId() != null ? UUID.fromString(document.getCategoryId()) : null)
                .commentCount(document.getCommentCount())
                .likeCount(document.getLikeCount())
                .dislikeCount(document.getDislikeCount())
                .build();
    }

    /**
     * PostDetailProjection을 PostDetailDocument로 변환
     * Document와 Projection은 동일한 필드 구조를 유지
     */
    public PostDetailDocument toDocument(PostDetailProjection projection) {
        if (projection == null) {
            return null;
        }

        return PostDetailDocument.create(
                projection.getId(),
                projection.getTitle(),
                projection.getContent(),
                projection.getAuthorId(),
                projection.getAuthorType(),
                projection.getStatus(),
                projection.getThumbnailImageUrl(),
                projection.getCategoryId(),
                projection.getViewCount(),
                projection.getCommentCount(),
                projection.getLikeCount(),
                projection.getDislikeCount(),
                projection.getCreatedAt(),
                projection.getUpdatedAt()
        );
    }
}
