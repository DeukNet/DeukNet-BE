package org.example.deuknetinfrastructure.common.seedwork;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.Persistable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Elasticsearch Document의 기반 클래스
 * UUID 기반 식별자와 생성/수정 시각을 포함합니다.
 *
 * 사용하려면:
 * - @Document 어노테이션을 서브클래스에 추가
 * - indexName을 지정
 *
 * 예시:
 * <pre>
 * {@code
 * @Document(indexName = "posts")
 * public class PostDocument extends BaseDocument {
 *     // 필드 추가
 * }
 * }
 * </pre>
 */
@Getter
public abstract class BaseDocument implements Persistable {

    /**
     * Document의 고유 식별자
     * Elasticsearch의 _id로 매핑됩니다.
     */
    @Id
    private String id;

    /**
     * Document가 생성된 시각
     * ISO 8601 형식으로 저장됩니다.
     */
    @CreatedDate
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    /**
     * Document가 마지막으로 수정된 시각
     * ISO 8601 형식으로 저장됩니다.
     */
    @LastModifiedDate
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updatedAt;

    protected BaseDocument() {
        // Elasticsearch를 위한 기본 생성자
    }

    protected BaseDocument(String id) {
        this.id = id;
    }

    protected BaseDocument(UUID id) {
        this.id = id != null ? id.toString() : null;
    }

    /**
     * UUID 타입의 ID를 반환합니다.
     * Domain layer와의 호환성을 위해 제공됩니다.
     */
    @Override
    public UUID getId() {
        return id != null ? UUID.fromString(id) : null;
    }

    /**
     * String 타입의 ID를 반환합니다.
     * Elasticsearch의 _id와 직접 매핑됩니다.
     */
    public String getIdAsString() {
        return id;
    }

    /**
     * ID를 설정합니다.
     */
    protected void setId(String id) {
        this.id = id;
    }

    /**
     * UUID로 ID를 설정합니다.
     */
    protected void setId(UUID id) {
        this.id = id != null ? id.toString() : null;
    }

    /**
     * ID 기반 동등성 비교
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseDocument)) return false;
        BaseDocument that = (BaseDocument) o;
        return Objects.equals(id, that.id);
    }

    /**
     * ID 기반 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
