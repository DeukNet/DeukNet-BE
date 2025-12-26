package org.example.deuknetinfrastructure.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Elasticsearch 설정
 * Spring Data Elasticsearch가 Repository를 통해 인덱스를 자동으로 생성하도록 합니다.
 * LocalDateTime 등 Java 8 Time API 지원을 위해 ObjectMapper를 커스터마이징합니다.
 */
@Configuration
@EnableRetry
public class ElasticsearchConfig {

    /**
     * ElasticsearchClient 빈 생성
     * JavaTimeModule이 등록된 ObjectMapper를 사용하도록 설정합니다.
     * Connection pool 설정과 재시도 로직을 포함합니다.
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(
            ElasticsearchProperties elasticsearchProperties) {

        String uris = elasticsearchProperties.getUris().get(0);
        HttpHost host = HttpHost.create(uris);

        // Elasticsearch 인증 정보 설정
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (elasticsearchProperties.getUsername() != null && elasticsearchProperties.getPassword() != null) {
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            elasticsearchProperties.getUsername(),
                            elasticsearchProperties.getPassword()
                    )
            );
        }

        // RestClient에 connection pool 및 인증 설정 추가
        // 테스트 환경에서 연결이 끊어지는 문제를 방지하기 위해 큰 타임아웃 설정
        RestClient restClient = RestClient.builder(host)
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                                .setConnectTimeout(10000)  // 연결 타임아웃 10초
                                .setSocketTimeout(60000)  // 소켓 타임아웃 60초
                )
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider)  // 인증 정보 추가
                                .setMaxConnTotal(100)  // 최대 연결 수
                                .setMaxConnPerRoute(100)  // 라우트당 최대 연결 수
                )
                .build();

        // JavaTimeModule이 등록된 ObjectMapper 생성
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // LocalDateTime을 ISO-8601 문자열로 직렬화 (배열이 아닌)
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();  // 다른 모듈도 자동 등록

        // JacksonJsonpMapper에 커스텀 ObjectMapper 제공
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(mapper)
        );

        return new ElasticsearchClient(transport);
    }

    /**
     * ElasticsearchOperations 빈 생성
     * Repository가 인덱스를 자동으로 생성하는데 필요합니다.
     */
    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
    public ElasticsearchOperations elasticsearchOperations(
            ElasticsearchClient elasticsearchClient,
            ElasticsearchConverter elasticsearchConverter) {
        return new ElasticsearchTemplate(elasticsearchClient, elasticsearchConverter);
    }

    /**
     * ElasticsearchConverter 빈 생성
     */
    @Bean
    public ElasticsearchConverter elasticsearchConverter() {
        return new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
    }
}
