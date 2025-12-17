package org.example.deuknetinfrastructure.data.debezium;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Debezium Offset 저장 Entity
 * <br>
 * FileOffsetBackingStore를 대체하여 DB에 offset을 저장합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "debezium_offset_storage")
public class DebeziumOffsetEntity {

    @Id
    @Column(length = 255)
    private String id;

    @Column(name = "offset_key", nullable = false, columnDefinition = "TEXT")
    private String offsetKey;

    @Column(name = "offset_value", nullable = false, columnDefinition = "TEXT")
    private String offsetValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DebeziumOffsetEntity(String id, String offsetKey, String offsetValue) {
        this.id = id;
        this.offsetKey = offsetKey;
        this.offsetValue = offsetValue;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
