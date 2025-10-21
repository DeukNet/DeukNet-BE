package org.example.deuknetinfrastructure.data.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

/**
 * 카테고리 검색용 Elasticsearch Document
 *
 * 카테고리 검색 및 계층 구조 조회에 최적화되었습니다.
 * - 이름: 검색 대상
 * - 부모 카테고리: 필터링 및 계층 구조
 * - 통계: 정렬 및 집계
 */
@Getter
@Setter
@Document(indexName = "categories")
public class CategoryDocument extends BaseDocument {

    /**
     * 카테고리 이름 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    /**
     * 부모 카테고리 ID (필터링용, 계층 구조)
     */
    @Field(type = FieldType.Keyword)
    private String parentCategoryId;

    /**
     * 루트 카테고리 여부
     */
    @Field(type = FieldType.Boolean)
    private Boolean isRootCategory;

    /**
     * 카테고리 경로 (검색 및 필터링)
     * 예: "개발/백엔드/Java"
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String path;

    /**
     * 계층 깊이 (정렬 및 필터링)
     * 0: 루트, 1: 1단계, 2: 2단계, ...
     */
    @Field(type = FieldType.Integer)
    private Integer depth;

    /**
     * 해당 카테고리의 게시글 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long postCount;

    /**
     * 하위 카테고리 포함 게시글 수 (정렬 및 집계)
     */
    @Field(type = FieldType.Long)
    private Long totalPostCount;

    protected CategoryDocument() {
        super();
    }

    public CategoryDocument(UUID id) {
        super(id);
    }

    public static CategoryDocument create(UUID id, String name, UUID parentCategoryId,
                                          Boolean isRootCategory, String path, Integer depth,
                                          Long postCount, Long totalPostCount) {
        CategoryDocument document = new CategoryDocument(id);
        document.name = name;
        document.parentCategoryId = parentCategoryId != null ? parentCategoryId.toString() : null;
        document.isRootCategory = isRootCategory;
        document.path = path;
        document.depth = depth;
        document.postCount = postCount != null ? postCount : 0L;
        document.totalPostCount = totalPostCount != null ? totalPostCount : 0L;
        return document;
    }
}
