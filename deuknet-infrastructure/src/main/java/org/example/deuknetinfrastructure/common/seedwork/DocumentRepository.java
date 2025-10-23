package org.example.deuknetinfrastructure.common.seedwork;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

/**
 * Elasticsearch Document Repository 추상화
 *
 * 모든 Elasticsearch Document Repository의 기반 인터페이스입니다.
 * Document 읽기/쓰기 로직을 캡슐화하여 인프라 계층의 세부사항을 숨깁니다.
 *
 * 주요 기능:
 * - UUID 기반 조회 (String으로 자동 변환)
 * - Document 저장/삭제
 * - Dirty Checking (향후 구현 예정)
 *
 * @param <T> Document 타입 (BaseDocument 상속)
 */
@NoRepositoryBean
public interface DocumentRepository<T extends BaseDocument> extends ElasticsearchRepository<T, String> {

    /**
     * UUID로 Document 조회
     *
     * Elasticsearch는 String ID를 사용하므로 UUID를 String으로 변환합니다.
     *
     * @param id Document ID (UUID)
     * @return Document (Optional)
     */
    default Optional<T> findByUuid(UUID id) {
        return findById(id.toString());
    }

    /**
     * Document 저장 (캡슐화)
     *
     * Elasticsearch에 Document를 저장합니다.
     * 향후 Dirty Checking 로직이 추가될 예정입니다.
     *
     * @param document 저장할 Document
     * @return 저장된 Document
     */
    default T saveDocument(T document) {
        return save(document);
    }

    /**
     * UUID로 Document 삭제
     *
     * @param id Document ID (UUID)
     */
    default void deleteByUuid(UUID id) {
        deleteById(id.toString());
    }

    /**
     * Document 존재 여부 확인 (UUID)
     *
     * @param id Document ID (UUID)
     * @return 존재하면 true
     */
    default boolean existsByUuid(UUID id) {
        return existsById(id.toString());
    }
}
