package org.example.deuknetinfrastructure.common.seedwork;

/**
 * 검색 가능한 Document를 나타내는 마커 인터페이스
 * Elasticsearch에서 전문 검색(Full-text Search)이 가능한 Document에 사용합니다.
 *
 * 이 인터페이스를 구현하는 Document는:
 * - 검색 인덱스에 저장됨
 * - 전문 검색 쿼리의 대상이 됨
 * - 검색 결과로 반환될 수 있음
 *
 * 사용 예:
 * <pre>
 * {@code
 * @Document(indexName = "posts")
 * public class PostDocument extends AuditableDocument implements SearchableDocument {
 *
 *     @Field(type = FieldType.Text, analyzer = "nori")
 *     private String title;
 *
 *     @Field(type = FieldType.Text, analyzer = "nori")
 *     private String content;
 *
 *     @Override
 *     public String getSearchableContent() {
 *         return title + " " + content;
 *     }
 * }
 * }
 * </pre>
 */
public interface SearchableDocument {

    /**
     * 검색 대상이 되는 주요 콘텐츠를 반환합니다.
     * 여러 필드를 조합하여 검색 가능한 단일 문자열로 반환할 수 있습니다.
     *
     * @return 검색 대상 콘텐츠
     */
    default String getSearchableContent() {
        return "";
    }

    /**
     * Document의 검색 가중치를 반환합니다.
     * 기본값은 1.0이며, 더 중요한 문서일수록 높은 값을 반환합니다.
     *
     * @return 검색 가중치 (1.0 = 기본)
     */
    default Float getSearchWeight() {
        return 1.0f;
    }

    /**
     * Document가 검색 가능한 상태인지 확인합니다.
     * 예를 들어, 삭제되었거나 비공개 상태인 문서는 false를 반환할 수 있습니다.
     *
     * @return 검색 가능하면 true
     */
    default boolean isSearchable() {
        return true;
    }
}
