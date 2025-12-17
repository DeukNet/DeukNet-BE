package org.example.deuknetinfrastructure.external.messaging.debezium;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.runtime.WorkerConfig;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.apache.kafka.connect.util.Callback;
import org.example.deuknetinfrastructure.data.debezium.DebeziumOffsetEntity;
import org.example.deuknetinfrastructure.data.debezium.DebeziumOffsetJpaRepository;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Database 기반 Offset 저장소
 * <br>
 * FileOffsetBackingStore 대신 PostgreSQL 테이블에 offset을 저장합니다.
 * <br>
 * Multi-instance 환경에서 안전하게 동작합니다.
 * <br>
 * Debezium은 리플렉션으로 이 클래스를 인스턴스화하므로 기본 생성자가 필요합니다.
 * Spring Bean은 static 필드로 주입받습니다.
 */
@Slf4j
public class DatabaseOffsetBackingStore extends MemoryOffsetBackingStore {

    private static DebeziumOffsetJpaRepository staticOffsetRepository;
    private final ObjectMapper objectMapper;

    /**
     * Debezium이 리플렉션으로 호출하는 기본 생성자
     */
    public DatabaseOffsetBackingStore() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Spring이 Bean을 주입할 수 있도록 static setter 제공
     */
    public static void setOffsetRepository(DebeziumOffsetJpaRepository offsetRepository) {
        staticOffsetRepository = offsetRepository;
    }

    @Override
    public void configure(WorkerConfig config) {
        super.configure(config);
        log.info("DatabaseOffsetBackingStore configured");
    }

    @Override
    public void start() {
        super.start();
        log.info("DatabaseOffsetBackingStore started - loading offsets from database");
        loadFromDatabase();
    }

    @Override
    public void stop() {
        super.stop();
        log.info("DatabaseOffsetBackingStore stopped");
    }

    @Override
    public Future<Void> set(Map<ByteBuffer, ByteBuffer> values, Callback<Void> callback) {
        return executor.submit(() -> {
            try {
                // 메모리에 먼저 저장
                for (Map.Entry<ByteBuffer, ByteBuffer> entry : values.entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }

                // DB에 저장
                saveToDatabase(values);

                if (callback != null) {
                    callback.onCompletion(null, null);
                }
            } catch (Exception e) {
                log.error("Error saving offset to database", e);
                if (callback != null) {
                    callback.onCompletion(e, null);
                }
            }
            return null;
        });
    }

    @Override
    public Future<Map<ByteBuffer, ByteBuffer>> get(Collection<ByteBuffer> keys) {
        return executor.submit(() -> {
            Map<ByteBuffer, ByteBuffer> result = new HashMap<>();
            for (ByteBuffer key : keys) {
                ByteBuffer value = data.get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
            return result;
        });
    }

    @Override
    public Set<Map<String, Object>> connectorPartitions(String connectorName) {
        // Debezium Embedded Engine에서는 사용하지 않음
        return Collections.emptySet();
    }

    /**
     * Database에서 offset 로드
     */
    private void loadFromDatabase() {
        try {
            if (staticOffsetRepository == null) {
                log.warn("OffsetRepository not set. Cannot load offsets from database.");
                return;
            }

            List<DebeziumOffsetEntity> entities = staticOffsetRepository.findAll();

            for (DebeziumOffsetEntity entity : entities) {
                String keyJson = entity.getOffsetKey();
                String valueJson = entity.getOffsetValue();

                ByteBuffer key = ByteBuffer.wrap(keyJson.getBytes(StandardCharsets.UTF_8));
                ByteBuffer value = ByteBuffer.wrap(valueJson.getBytes(StandardCharsets.UTF_8));

                data.put(key, value);
                log.debug("Loaded offset from database: key={}", keyJson);
            }

            log.info("Successfully loaded {} offset entries from database", data.size());
        } catch (Exception e) {
            log.error("Error loading offsets from database", e);
        }
    }

    /**
     * Database에 offset 저장
     */
    private void saveToDatabase(Map<ByteBuffer, ByteBuffer> values) {
        try {
            if (staticOffsetRepository == null) {
                log.error("OffsetRepository not set. Cannot save offsets to database.");
                throw new IllegalStateException("OffsetRepository not initialized");
            }

            for (Map.Entry<ByteBuffer, ByteBuffer> entry : values.entrySet()) {
                String keyJson = new String(entry.getKey().array(), StandardCharsets.UTF_8);
                String valueJson = new String(entry.getValue().array(), StandardCharsets.UTF_8);

                // 키의 해시값을 ID로 사용
                String id = generateId(keyJson);

                // JPA를 사용한 Upsert (findById + save)
                DebeziumOffsetEntity entity = staticOffsetRepository.findById(id)
                        .orElse(new DebeziumOffsetEntity(id, keyJson, valueJson));

                entity.setOffsetValue(valueJson);
                staticOffsetRepository.save(entity);

                log.debug("Saved offset to database: id={}, key={}", id, keyJson);
            }

            log.info("Successfully saved {} offset entries to database", values.size());
        } catch (Exception e) {
            log.error("Error saving offsets to database", e);
            throw new RuntimeException("Failed to save offsets to database", e);
        }
    }

    /**
     * Offset 키에서 고유 ID 생성 (SHA-256 해시)
     */
    private String generateId(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));

            // 해시를 HEX 문자열로 변환
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
            // Fallback: 키를 그대로 사용 (최대 255자로 제한)
            return key.length() > 255 ? key.substring(0, 255) : key;
        }
    }
}
