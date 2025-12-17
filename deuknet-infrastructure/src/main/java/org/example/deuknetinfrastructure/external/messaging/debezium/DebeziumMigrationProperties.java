package org.example.deuknetinfrastructure.external.messaging.debezium;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Debezium Offset 마이그레이션 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "debezium.migration")
public class DebeziumMigrationProperties {

    /**
     * 마이그레이션 활성화 여부
     */
    private boolean enabled = false;

    /**
     * 마이그레이션할 파일 경로 (기존 offsets.dat 파일)
     */
    private String filePath = "offsets.dat";
}
