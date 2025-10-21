package org.example.deuknetinfrastructure.data.document;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetinfrastructure.common.seedwork.BaseDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 카테고리 계층 구조 검색용 Elasticsearch Document
 *
 * 카테고리와 하위 카테고리의 계층 구조를 표현하는 Document입니다.
 * 중첩된 children 필드를 통해 전체 카테고리 트리를 한 번의 조회로 가져올 수 있습니다.
 *
 * 사용 예:
 * - 카테고리 네비게이션 트리 조회
 * - 계층 구조 기반 검색
 * - 하위 카테고리 포함 전체 검색
 * - 카테고리 경로 추적
 *
 * 필드:
 * - 카테고리 이름: 형태소 분석을 통한 한글 검색
 * - 부모-자식 관계: 계층 구조 표현
 * - 중첩된 children: 하위 카테고리 구조
 * - 통계 정보: 게시글 수 등
 */
@Getter
@Setter
@Document(indexName = "categories-tree")
public class CategoryTreeDocument extends BaseDocument {

    /**
     * 카테고리 이름 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    /**
     * 카테고리 설명 (검색 대상)
     */
    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    /**
     * 부모 카테고리 ID (최상위 카테고리의 경우 null)
     */
    @Field(type = FieldType.Keyword)
    private String parentId;

    /**
     * 카테고리 깊이 (0: 최상위 카테고리, 1: 1차 하위, ...)
     */
    @Field(type = FieldType.Integer)
    private Integer depth;

    /**
     * 카테고리 순서 (같은 레벨 내에서의 정렬)
     */
    @Field(type = FieldType.Integer)
    private Integer orderIndex;

    /**
     * 카테고리 상태 (필터링용)
     * ACTIVE, INACTIVE
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 카테고리 경로 (예: "Tech/Backend/Java")
     * 전체 경로를 통한 검색 및 필터링에 사용
     */
    @Field(type = FieldType.Keyword)
    private String path;

    /**
     * 게시글 수 (집계 및 정렬)
     * 하위 카테고리의 게시글을 포함한 총 개수
     */
    @Field(type = FieldType.Long)
    private Long postCount;

    /**
     * 직접 소속된 게시글 수 (집계)
     * 하위 카테고리 제외, 이 카테고리에만 속한 게시글 수
     */
    @Field(type = FieldType.Long)
    private Long directPostCount;

    /**
     * 하위 카테고리 목록 (중첩 구조)
     * Nested type으로 계층 구조 표현
     */
    @Field(type = FieldType.Nested)
    private List<CategoryTreeDocument> children;

    protected CategoryTreeDocument() {
        super();
        this.children = new ArrayList<>();
    }

    public CategoryTreeDocument(UUID id) {
        super(id);
        this.children = new ArrayList<>();
    }

    public static CategoryTreeDocument create(UUID id, String name, String description,
                                              UUID parentId, Integer depth, Integer orderIndex,
                                              String status, String path,
                                              Long postCount, Long directPostCount) {
        CategoryTreeDocument document = new CategoryTreeDocument(id);
        document.name = name;
        document.description = description;
        document.parentId = parentId != null ? parentId.toString() : null;
        document.depth = depth;
        document.orderIndex = orderIndex;
        document.status = status;
        document.path = path;
        document.postCount = postCount;
        document.directPostCount = directPostCount;
        return document;
    }

    /**
     * 하위 카테고리 추가
     */
    public void addChild(CategoryTreeDocument child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }

    /**
     * 전체 하위 카테고리 수 (재귀적으로 계산)
     */
    public int getTotalChildCount() {
        int count = this.children.size();
        for (CategoryTreeDocument child : this.children) {
            count += child.getTotalChildCount();
        }
        return count;
    }

    /**
     * 최대 깊이 계산 (재귀적으로 계산)
     */
    public int getMaxDepth() {
        if (this.children.isEmpty()) {
            return this.depth;
        }
        int maxChildDepth = this.depth;
        for (CategoryTreeDocument child : this.children) {
            maxChildDepth = Math.max(maxChildDepth, child.getMaxDepth());
        }
        return maxChildDepth;
    }

    /**
     * 리프 카테고리 여부 (하위 카테고리가 없는 경우)
     */
    public boolean isLeaf() {
        return this.children == null || this.children.isEmpty();
    }

    /**
     * 루트 카테고리 여부 (부모가 없는 경우)
     */
    public boolean isRoot() {
        return this.parentId == null;
    }
}
