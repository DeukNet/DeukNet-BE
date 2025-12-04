package org.example.seedwork;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;

/**
 * Elasticsearch TestContainer 싱글톤
 *
 * 테스트 간에 Elasticsearch 컨테이너를 재사용하여 테스트 속도를 향상시킵니다.
 * Nori 플러그인이 설치된 커스텀 이미지를 사용합니다.
 */
public class TestElasticsearchContainer {

    private static GenericContainer<?> container;

    private TestElasticsearchContainer() {
    }

    public static GenericContainer<?> getInstance() {
        if (container == null) {
            // 프로젝트 루트 찾기 (user.dir은 gradle 프로젝트 루트)
            String projectRoot = System.getProperty("user.dir");
            // presentation 모듈에서 실행되면 한 단계 위로
            if (projectRoot.endsWith("deuknet-presentation")) {
                projectRoot = Paths.get(projectRoot).getParent().toString();
            }

            java.nio.file.Path dockerfilePath = Paths.get(projectRoot, "docker", "Dockerfile.elasticsearch");

            // 프로젝트 루트에서 docker/Dockerfile.elasticsearch를 사용하여 이미지 빌드
            ImageFromDockerfile customImage = new ImageFromDockerfile("deuknet-elasticsearch-test", false)
                    .withDockerfile(dockerfilePath);

            container = new GenericContainer<>(customImage)
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withExposedPorts(9200, 9300)
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
