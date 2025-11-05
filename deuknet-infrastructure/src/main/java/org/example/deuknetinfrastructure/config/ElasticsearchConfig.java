package org.example.deuknetinfrastructure.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

/**
 * Elasticsearch 설정
 * Spring Data Elasticsearch가 Repository를 통해 인덱스를 자동으로 생성하도록 합니다.
 */
@Configuration
public class ElasticsearchConfig {

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
