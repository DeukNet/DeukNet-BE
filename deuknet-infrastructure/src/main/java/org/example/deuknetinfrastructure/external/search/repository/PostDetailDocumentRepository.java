package org.example.deuknetinfrastructure.external.search.repository;

import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * PostDetailDocument용 Elasticsearch Repository
 *
 * 이 Repository는 Spring이 PostDetailDocument를 인식하고
 * 자동으로 인덱스를 생성하도록 하기 위한 용도입니다.
 *
 * 프로덕션에서는 CDC를 통해 Elasticsearch에 데이터가 동기화되므로
 * 이 Repository를 직접 사용하지 않습니다.
 */
@Repository
public interface PostDetailDocumentRepository extends ElasticsearchRepository<PostDetailDocument, UUID> {
}
