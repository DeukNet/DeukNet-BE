package org.example.deuknetinfrastructure.data.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.UUID;

/**
 * 게시글 상세 검색용 Elasticsearch Document
 *
 * 게시글 상세 조회 및 전문 검색(Full-text search)에 최적화된 구조입니다.
 * 모든 필드를 포함하여 상세 검색 및 표시를 지원합니다.
 *
 * 사용 예:
 * - 게시글 상세 페이지 검색
 * - 본문 전체 검색
 * - 상세 필터링 (카테고리, 작성자, 태그 등)
 *
 * 필드:
 * - 제목, 내용: 형태소 분석을 통한 한글 검색
 * - 작성자 정보: 필터링 및 검색
 * - 카테고리: 필터링
 * - 통계: 정렬 및 집계
 */
@Getter
@Setter
@Document(indexName = "posts-detail")
public class PostDetailDocument extends BaseDocument {

    /**
     * 게시글 제목 (검색 대상)
     * nori 분석기를 사용한 한글 형태소 분석
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    /**
     * 게시글 내용 (검색 대상)
     * nori 분석기를 사용한 한글 형태소 분석
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
     * 게시글 상태 (필터링용)
     * DRAFT, PUBLISHED, ARCHIVED, DELETED
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 카테고리 ID 목록 (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private List<String> categoryIds;

    /**
     * 카테고리 이름 목록 (검색 및 표시용)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> categoryNames;

    /**
     * 조회수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long viewCount;

    /**
     * 댓글 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long commentCount;

    /**
     * 좋아요 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long likeCount;

    protected PostDetailDocument() {
        super();
    }

    public PostDetailDocument(UUID id) {
        super(id);
    }

    public static PostDetailDocument create(UUID id, String title, String content,
                                            UUID authorId, String authorUsername, String authorDisplayName,
                                            String status, List<UUID> categoryIds, List<String> categoryNames,
                                            Long viewCount, Long commentCount, Long likeCount) {
        PostDetailDocument document = new PostDetailDocument(id);
        document.title = title;
        document.content = content;
        document.authorId = authorId.toString();
        document.authorUsername = authorUsername;
        document.authorDisplayName = authorDisplayName;
        document.status = status;
        document.categoryIds = categoryIds != null ? categoryIds.stream().map(UUID::toString).toList() : List.of();
        document.categoryNames = categoryNames != null ? categoryNames : List.of();
        document.viewCount = viewCount;
        document.commentCount = commentCount;
        document.likeCount = likeCount;
        return document;
    }
}
