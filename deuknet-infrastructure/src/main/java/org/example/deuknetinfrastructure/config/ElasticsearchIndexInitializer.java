package org.example.deuknetinfrastructure.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.StringReader;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchClient elasticsearchClient;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndices() {
        createPostsDetailIndex();
    }

    private void createPostsDetailIndex() {
        String indexName = "posts-detail";

        try {
            // 인덱스 존재 여부 확인
            boolean exists = elasticsearchClient.indices()
                .exists(ExistsRequest.of(e -> e.index(indexName)))
                .value();

            if (exists) {
                log.info("Index '{}' already exists", indexName);
                return;
            }

            // 인덱스 생성
            String mappingJson = """
                {
                  "properties": {
                    "id": { "type": "keyword" },
                    "title": {
                      "type": "text",
                      "analyzer": "nori_analyzer",
                      "fields": {
                        "autocomplete": {
                          "type": "text",
                          "analyzer": "edge_ngram_analyzer",
                          "search_analyzer": "edge_ngram_search_analyzer"
                        }
                      }
                    },
                    "content": {
                      "type": "text",
                      "analyzer": "nori_analyzer",
                      "fields": {
                        "autocomplete": {
                          "type": "text",
                          "analyzer": "edge_ngram_analyzer",
                          "search_analyzer": "edge_ngram_search_analyzer"
                        }
                      }
                    },
                    "authorId": { "type": "keyword" },
                    "authorUsername": { "type": "keyword" },
                    "authorDisplayName": { "type": "text" },
                    "status": { "type": "keyword" },
                    "categoryIds": { "type": "keyword" },
                    "categoryNames": { "type": "text" },
                    "viewCount": { "type": "long" },
                    "commentCount": { "type": "long" },
                    "likeCount": { "type": "long" },
                    "dislikeCount": { "type": "long" },
                    "createdAt": {
                      "type": "date",
                      "format": "date_hour_minute_second_millis"
                    },
                    "updatedAt": {
                      "type": "date",
                      "format": "date_hour_minute_second_millis"
                    }
                  }
                }
                """;

            String settingsJson = """
                {
                  "analysis": {
                    "tokenizer": {
                      "edge_ngram_tokenizer": {
                        "type": "edge_ngram",
                        "min_gram": 1,
                        "max_gram": 10,
                        "token_chars": ["letter", "digit"]
                      },
                      "nori_tokenizer": {
                        "type": "nori_tokenizer",
                        "decompound_mode": "mixed"
                      }
                    },
                    "analyzer": {
                      "nori_analyzer": {
                        "type": "custom",
                        "tokenizer": "nori_tokenizer",
                        "filter": ["lowercase", "nori_readingform"]
                      },
                      "edge_ngram_analyzer": {
                        "type": "custom",
                        "tokenizer": "edge_ngram_tokenizer",
                        "filter": ["lowercase"]
                      },
                      "edge_ngram_search_analyzer": {
                        "type": "custom",
                        "tokenizer": "standard",
                        "filter": ["lowercase"]
                      }
                    }
                  }
                }
                """;

            elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c
                .index(indexName)
                .mappings(m -> m.withJson(new StringReader(mappingJson)))
                .settings(s -> s.withJson(new StringReader(settingsJson)))
            ));

            log.info("Successfully created index '{}'", indexName);

        } catch (Exception e) {
            log.error("Failed to create index '{}'", indexName, e);
        }
    }
}
