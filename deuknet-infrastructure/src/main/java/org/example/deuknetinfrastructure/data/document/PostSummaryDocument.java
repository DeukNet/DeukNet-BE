package org.example.deuknetinfrastructure.data.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

/**
 * 게시글 목록 검색용 Elasticsearch Document
 *
 * 게시글 목록 조회에 최적화된 경량 Document입니다.
 * 필요한 최소한의 필드만 포함하여 검색 성능을 향상시킵니다.
 *
 * 사용 예:
 * - 게시글 목록 페이지
 * - 빠른 검색 결과 미리보기
 * - 카테고리별 게시글 목록
 */
@Getter
@Setter
@Document(indexName = "posts-summary")
public class PostSummaryDocument extends BaseDocument {

    /**
     * 게시글 제목 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    /**
     * 작성자 ID (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String authorId;

    /**
     * 작성자 displayName (검색 및 표시용)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String authorDisplayName;

    /**
     * 게시글 상태 (필터링용)
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 조회수 (정렬용)
     */
    @Field(type = FieldType.Long)
    private Long viewCount;

    /**
     * 댓글 수 (정렬 및 표시용)
     */
    @Field(type = FieldType.Long)
    private Long commentCount;

    protected PostSummaryDocument() {
        super();
    }

    public PostSummaryDocument(UUID id) {
        super(id);
    }

    public static PostSummaryDocument create(UUID id, String title,
                                             UUID authorId, String authorDisplayName,
                                             String status, Long viewCount, Long commentCount) {
        PostSummaryDocument document = new PostSummaryDocument(id);
        document.title = title;
        document.authorId = authorId.toString();
        document.authorDisplayName = authorDisplayName;
        document.status = status;
        document.viewCount = viewCount;
        document.commentCount = commentCount;
        return document;
    }
}
