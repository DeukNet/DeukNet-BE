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
     * User 정보는 Service Layer에서 별도로 조회하여 설정해야 함
     */
    public PostDetailProjection toProjection(PostDetailDocument document, String authorUsername, String authorDisplayName, String authorAvatarUrl) {
        if (document == null) {
            return null;
        }

        return PostDetailProjection.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .authorId(document.getAuthorId() != null ? UUID.fromString(document.getAuthorId()) : null)
                .authorUsername(authorUsername)
                .authorDisplayName(authorDisplayName)
                .authorAvatarUrl(authorAvatarUrl)
                .authorType(document.getAuthorType())
                .status(document.getStatus())
                .viewCount(document.getViewCount())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .categoryId(document.getCategoryId() != null ? UUID.fromString(document.getCategoryId()) : null)
                .categoryName(document.getCategoryName())
                .commentCount(document.getCommentCount())
                .likeCount(document.getLikeCount())
                .dislikeCount(document.getDislikeCount())
                .build();
    }

    /**
     * PostDetailProjection을 PostDetailDocument로 변환
     * User 정보는 Document에 저장하지 않음 (authorId, authorType만 저장)
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
                projection.getCategoryId(),
                projection.getCategoryName(),
                projection.getViewCount(),
                projection.getCommentCount(),
                projection.getLikeCount(),
                projection.getDislikeCount(),
                projection.getCreatedAt(),
                projection.getUpdatedAt()
        );
    }
}
