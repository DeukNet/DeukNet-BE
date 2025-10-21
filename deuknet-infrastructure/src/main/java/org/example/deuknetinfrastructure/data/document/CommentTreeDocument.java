package org.example.deuknetinfrastructure.data.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 댓글 계층 구조 검색용 Elasticsearch Document
 *
 * 댓글과 대댓글의 계층 구조를 표현하는 Document입니다.
 * 중첩된 replies 필드를 통해 전체 댓글 트리를 한 번의 조회로 가져올 수 있습니다.
 *
 * 사용 예:
 * - 댓글 스레드 전체 조회
 * - 계층 구조 기반 검색
 * - 대댓글 포함 전문 검색
 *
 * 필드:
 * - 댓글 내용: 형태소 분석을 통한 한글 검색
 * - 작성자 정보: 필터링 및 검색
 * - 중첩된 replies: 대댓글 구조
 */
@Getter
@Setter
@Document(indexName = "comments-tree")
public class CommentTreeDocument extends BaseDocument {

    /**
     * 게시글 ID (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String postId;

    /**
     * 부모 댓글 ID (최상위 댓글의 경우 null)
     */
    @Field(type = FieldType.Keyword)
    private String parentId;

    /**
     * 댓글 내용 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    /**
     * 작성자 ID (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String authorId;

    /**
     * 작성자 username (검색 및 필터링)
     */
    @Field(type = FieldType.Keyword)
    private String authorUsername;

    /**
     * 작성자 displayName (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String authorDisplayName;

    /**
     * 댓글 상태 (필터링용)
     * ACTIVE, DELETED
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 댓글 깊이 (0: 최상위 댓글, 1: 대댓글, ...)
     */
    @Field(type = FieldType.Integer)
    private Integer depth;

    /**
     * 좋아요 수 (정렬용)
     */
    @Field(type = FieldType.Long)
    private Long likeCount;

    /**
     * 생성 시각 (정렬용)
     */
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    /**
     * 수정 시각 (정렬용)
     */
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    /**
     * 대댓글 목록 (중첩 구조)
     * Nested type으로 계층 구조 표현
     */
    @Field(type = FieldType.Nested)
    private List<CommentTreeDocument> replies;

    protected CommentTreeDocument() {
        super();
        this.replies = new ArrayList<>();
    }

    public CommentTreeDocument(UUID id) {
        super(id);
        this.replies = new ArrayList<>();
    }

    public static CommentTreeDocument create(UUID id, UUID postId, UUID parentId,
                                             String content, UUID authorId, String authorUsername,
                                             String authorDisplayName, String status, Integer depth,
                                             Long likeCount, LocalDateTime createdAt,
                                             LocalDateTime updatedAt) {
        CommentTreeDocument document = new CommentTreeDocument(id);
        document.postId = postId.toString();
        document.parentId = parentId != null ? parentId.toString() : null;
        document.content = content;
        document.authorId = authorId.toString();
        document.authorUsername = authorUsername;
        document.authorDisplayName = authorDisplayName;
        document.status = status;
        document.depth = depth;
        document.likeCount = likeCount;
        document.createdAt = createdAt;
        document.updatedAt = updatedAt;
        return document;
    }

    /**
     * 대댓글 추가
     */
    public void addReply(CommentTreeDocument reply) {
        if (this.replies == null) {
            this.replies = new ArrayList<>();
        }
        this.replies.add(reply);
    }

    /**
     * 전체 대댓글 수 (재귀적으로 계산)
     */
    public int getTotalReplyCount() {
        int count = this.replies.size();
        for (CommentTreeDocument reply : this.replies) {
            count += reply.getTotalReplyCount();
        }
        return count;
    }
}
