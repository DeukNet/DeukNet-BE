package org.example.seedwork;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgreSQLContainer {

    private static final String IMAGE_VERSION = "postgres:15-alpine";
    private static PostgreSQLContainer<?> container;

    private TestPostgreSQLContainer() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>(IMAGE_VERSION)
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    // CDC를 위한 WAL 설정
                    .withCommand(
                            "postgres",
                            "-c", "wal_level=logical",
                            "-c", "max_wal_senders=1",
                            "-c", "max_replication_slots=1"
                    )
                    .withReuse(false);
            container.start();
        }
        return container;
    }

    public static void stop() {
        if (container != null) {
            container.stop();
        }
    }
}
