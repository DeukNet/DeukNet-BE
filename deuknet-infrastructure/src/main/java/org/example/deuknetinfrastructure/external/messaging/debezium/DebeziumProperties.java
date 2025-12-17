package org.example.deuknetinfrastructure.external.messaging.debezium;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Debezium Embedded Engine 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "debezium")
public class DebeziumProperties {

    private boolean enabled = true;

    private String connectorName = "deuknet-outbox-connector";

    private Database database = new Database();

    @Getter
    @Setter
    public static class Database {
        private String hostname;
        private int port = 5432;
        private String name;
        private String username;
        private String password;
        private String serverName = "deuknet";  // topic.prefix
        private String schemaIncludeList = "public";
        private String tableIncludeList = "public.outbox_events";
    }
}
