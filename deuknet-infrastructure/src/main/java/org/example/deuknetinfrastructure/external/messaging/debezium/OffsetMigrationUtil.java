package org.example.deuknetinfrastructure.external.messaging.debezium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.deuknetinfrastructure.data.debezium.DebeziumOffsetEntity;
import org.example.deuknetinfrastructure.data.debezium.DebeziumOffsetJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;

/**
 * FileOffsetBackingStore의 offset을 Database로 마이그레이션하는 유틸리티
 * <br>
 * 사용법:
 * 1. application.yaml에 debezium.migration.enabled=true 설정
 * 2. debezium.migration.file-path에 기존 offsets.dat 파일 경로 지정
 * 3. 애플리케이션 시작 시 자동으로 마이그레이션 수행
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "debezium.migration.enabled", havingValue = "true")
public class OffsetMigrationUtil implements CommandLineRunner {

    private final DebeziumOffsetJpaRepository offsetRepository;
    private final ObjectMapper objectMapper;
    private final String offsetFilePath;

    public OffsetMigrationUtil(
            DebeziumOffsetJpaRepository offsetRepository,
            DebeziumMigrationProperties properties
    ) {
        this.offsetRepository = offsetRepository;
        this.objectMapper = new ObjectMapper();
        this.offsetFilePath = properties.getFilePath();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Starting Debezium Offset Migration ===");
        log.info("Source file: {}", offsetFilePath);

        File offsetFile = new File(offsetFilePath);
        if (!offsetFile.exists()) {
            log.warn("Offset file not found: {}. Skipping migration.", offsetFilePath);
            return;
        }

        try {
            migrateOffsets(offsetFile);
            log.info("=== Offset Migration Completed Successfully ===");
        } catch (Exception e) {
            log.error("Failed to migrate offsets", e);
            throw new RuntimeException("Offset migration failed", e);
        }
    }

    private void migrateOffsets(File offsetFile) throws IOException {
        // offsets.dat 파일 읽기 (JSON 형식)
        String content = Files.readString(offsetFile.toPath());
        JsonNode rootNode = objectMapper.readTree(content);

        int migratedCount = 0;

        // offset 데이터 파싱 및 DB 저장
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            String value = entry.getValue().toString();

            saveOffsetToDatabase(key, value);
            migratedCount++;
            log.debug("Migrated offset: key={}", key);
        }

        log.info("Successfully migrated {} offset entries to database", migratedCount);
    }

    private void saveOffsetToDatabase(String keyJson, String valueJson) {
        String id = generateId(keyJson);

        DebeziumOffsetEntity entity = offsetRepository.findById(id)
                .orElse(new DebeziumOffsetEntity(id, keyJson, valueJson));

        entity.setOffsetValue(valueJson);
        offsetRepository.save(entity);
    }

    private String generateId(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error generating ID from key: {}", key, e);
            return key.length() > 255 ? key.substring(0, 255) : key;
        }
    }
}
