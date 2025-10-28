package org.example.deuknetapplication.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 댓글 검색 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentSearchResponse {
    private UUID id;
    private UUID postId;
    private String content;
    private UUID authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;
    private UUID parentCommentId;
    private Boolean isReply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
