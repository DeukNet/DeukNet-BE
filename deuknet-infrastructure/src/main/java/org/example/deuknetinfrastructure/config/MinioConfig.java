package org.example.deuknetinfrastructure.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 설정
 */
@Configuration
public class MinioConfig {

    @Value("${minio.url:http://minio:9000}")
    private String minioUrl;

    @Value("${minio.external-url:http://172.17.0.3:30901}")
    private String minioExternalUrl;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin123}")
    private String secretKey;

    @Value("${minio.bucket-name:deuknet-files}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean("minioBucketName")
    public String minioBucketName() {
        return bucketName;
    }

    @Bean("minioExternalUrl")
    public String minioExternalUrl() {
        return minioExternalUrl;
    }
}
