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
     */
    public PostDetailProjection toProjection(PostDetailDocument document) {
        if (document == null) {
            return null;
        }

        return PostDetailProjection.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .authorId(document.getAuthorId() != null ? UUID.fromString(document.getAuthorId()) : null)
                .authorUsername(document.getAuthorUsername())
                .authorDisplayName(document.getAuthorDisplayName())
                .authorAvatarUrl(null) // Document에는 없는 필드
                .status(document.getStatus())
                .viewCount(document.getViewCount())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .categoryIds(document.getCategoryIds() != null ?
                    document.getCategoryIds().stream()
                        .map(UUID::fromString)
                        .toList() : List.of())
                .categoryNames(document.getCategoryNames())
                .commentCount(document.getCommentCount())
                .likeCount(document.getLikeCount())
                .dislikeCount(document.getDislikeCount())
                .build();
    }

    /**
     * PostDetailProjection을 PostDetailDocument로 변환
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
                projection.getAuthorUsername(),
                projection.getAuthorDisplayName(),
                projection.getStatus(),
                projection.getCategoryIds(),
                projection.getCategoryNames(),
                projection.getViewCount(),
                projection.getCommentCount(),
                projection.getLikeCount(),
                projection.getDislikeCount()
        );
    }
}
