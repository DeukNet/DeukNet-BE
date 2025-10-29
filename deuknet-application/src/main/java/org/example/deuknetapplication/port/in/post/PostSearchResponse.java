package org.example.deuknetapplication.port.in.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 게시글 검색 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchResponse {
    private UUID id;
    private String title;
    private String content;
    private UUID authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String status;
    private List<UUID> categoryIds;
    private List<String> categoryNames;
    private Long viewCount;
    private Long commentCount;
    private Long likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
