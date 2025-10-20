package org.example.deuknetinfrastructure.common.seedwork;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

/**
 * 감사(Audit) 정보를 포함하는 Elasticsearch Document의 기반 클래스
 * 생성/수정 시각뿐만 아니라 생성/수정한 사용자 정보도 자동으로 관리합니다.
 *
 * 사용하려면:
 * 1. @EnableElasticsearchAuditing을 설정 클래스에 추가
 * 2. AuditorAware<String> 구현체를 빈으로 등록
 *
 * 예시:
 * <pre>
 * {@code
 * @Bean
 * public AuditorAware<String> auditorProvider() {
 *     return () -> {
 *         UUID userId = SecurityUtil.getCurrentUserId();
 *         return Optional.ofNullable(userId).map(UUID::toString);
 *     };
 * }
 * }
 * </pre>
 *
 * 사용 예:
 * <pre>
 * {@code
 * @Document(indexName = "posts")
 * public class PostDocument extends AuditableDocument {
 *     @Field(type = FieldType.Text)
 *     private String title;
 *     // ...
 * }
 * }
 * </pre>
 */
@Getter
public abstract class AuditableDocument extends BaseDocument
        implements org.example.deuknetdomain.common.seedwork.Auditable {

    /**
     * Document를 생성한 사용자의 ID (UUID를 String으로 저장)
     */
    @CreatedBy
    @Field(type = FieldType.Keyword)
    private String createdBy;

    /**
     * Document를 마지막으로 수정한 사용자의 ID (UUID를 String으로 저장)
     */
    @LastModifiedBy
    @Field(type = FieldType.Keyword)
    private String updatedBy;

    protected AuditableDocument() {
        super();
    }

    protected AuditableDocument(String id) {
        super(id);
    }

    protected AuditableDocument(UUID id) {
        super(id);
    }

    /**
     * 생성한 사용자 ID를 UUID로 반환합니다.
     * Domain layer와의 호환성을 위해 제공됩니다.
     */
    @Override
    public UUID getCreatedBy() {
        return createdBy != null ? UUID.fromString(createdBy) : null;
    }

    /**
     * 수정한 사용자 ID를 UUID로 반환합니다.
     * Domain layer와의 호환성을 위해 제공됩니다.
     */
    @Override
    public UUID getUpdatedBy() {
        return updatedBy != null ? UUID.fromString(updatedBy) : null;
    }

    /**
     * 생성한 사용자 ID를 String으로 반환합니다.
     */
    public String getCreatedByAsString() {
        return createdBy;
    }

    /**
     * 수정한 사용자 ID를 String으로 반환합니다.
     */
    public String getUpdatedByAsString() {
        return updatedBy;
    }
}
