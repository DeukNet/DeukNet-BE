package org.example.deuknetapplication.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 검색 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {
    private UUID id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private Long postCount;
    private Long commentCount;
    private Long followerCount;
    private Long followingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
