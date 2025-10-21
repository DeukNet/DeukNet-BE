package org.example.seedwork;

import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * Elasticsearch TestContainer 싱글톤
 *
 * 테스트 간에 Elasticsearch 컨테이너를 재사용하여 테스트 속도를 향상시킵니다.
 */
public class TestElasticsearchContainer {

    private static final String IMAGE_VERSION = "docker.elastic.co/elasticsearch/elasticsearch:8.11.0";
    private static ElasticsearchContainer container;

    private TestElasticsearchContainer() {
    }

    public static ElasticsearchContainer getInstance() {
        if (container == null) {
            container = new ElasticsearchContainer(IMAGE_VERSION)
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
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
