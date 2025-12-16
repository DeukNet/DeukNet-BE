package org.example.deuknetinfrastructure.external.search.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.UUID;

/**
 * 게시글 상세 검색용 Elasticsearch Document
 * <br>
 * 게시글 상세 조회 및 전문 검색(Full-text search)에 최적화된 구조입니다.
 * 모든 필드를 포함하여 상세 검색 및 표시를 지원합니다.
 * <br>
 * 사용 예:
 * - 게시글 상세 페이지 검색
 * - 본문 전체 검색
 * - 상세 필터링 (카테고리, 작성자, 태그 등)
 * <br>
 * 필드:
 * - 제목, 내용: edge nGram을 통한 한글 자동완성 검색
 * - 작성자 정보: 필터링 및 검색
 * - 카테고리: 필터링
 * - 통계: 정렬 및 집계
 */
@Getter
@Setter
@Document(indexName = "posts-detail")
@Setting(settingPath = "elasticsearch/posts-detail-settings.json")
public class PostDetailDocument extends BaseDocument {

    /**
     * 게시글 제목 (검색 대상)
     * - 기본: Nori 형태소 분석 (조사/어미 제거, 어순 무관 검색)
     * - autocomplete: edge nGram (자동완성용)
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
        otherFields = {
            @InnerField(suffix = "autocomplete", type = FieldType.Text,
                        analyzer = "edge_ngram_analyzer",
                        searchAnalyzer = "edge_ngram_search_analyzer")
        }
    )
    private String title;

    /**
     * 게시글 내용 (검색 대상)
     * - 기본: Nori 형태소 분석 (조사/어미 제거, 어순 무관 검색)
     * - autocomplete: edge nGram (자동완성용)
     */
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
        otherFields = {
            @InnerField(suffix = "autocomplete", type = FieldType.Text,
                        analyzer = "edge_ngram_analyzer",
                        searchAnalyzer = "edge_ngram_search_analyzer")
        }
    )
    private String content;

    /**
     * 작성자 ID (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String authorId;

    /**
     * 작성자 타입 (필터링용)
     * REAL, ANONYMOUS
     */
    @Field(type = FieldType.Keyword)
    private String authorType;

    /**
     * 게시글 상태 (필터링용)
     * PRIVATE, PUBLIC, ARCHIVED, DELETED
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 썸네일 이미지 URL (검색 결과 표시용)
     */
    @Field(type = FieldType.Keyword)
    private String thumbnailImageUrl;

    /**
     * 카테고리 ID (필터링용, 이름은 별도 조회)
     */
    @Field(type = FieldType.Keyword)
    private String categoryId;

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

    /**
     * 싫어요 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long dislikeCount;

    public PostDetailDocument(UUID id) {
        super(id);
    }

    public static PostDetailDocument create(UUID id, String title, String content,
                                            UUID authorId, String authorType,
                                            String status, String thumbnailImageUrl, UUID categoryId,
                                            Long viewCount, Long commentCount, Long likeCount, Long dislikeCount,
                                            java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
        PostDetailDocument document = new PostDetailDocument(id);
        document.title = title;
        document.content = content;
        document.authorId = authorId.toString();
        document.authorType = authorType;
        document.status = status;
        document.thumbnailImageUrl = thumbnailImageUrl;
        document.categoryId = categoryId != null ? categoryId.toString() : null;
        document.viewCount = viewCount;
        document.commentCount = commentCount;
        document.likeCount = likeCount;
        document.dislikeCount = dislikeCount;
        document.setCreatedAt(createdAt);
        document.setUpdatedAt(updatedAt);
        return document;
    }
}
