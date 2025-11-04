package org.example.deuknetinfrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(
    scanBasePackages = {
        "org.example.deuknetinfrastructure",
        "org.example.deuknetpresentation",
        "org.example.deuknetapplication"
    },
    exclude = {UserDetailsServiceAutoConfiguration.class}
)
@EntityScan(basePackages = {
    "org.example.deuknetinfrastructure.data",
    "org.example.deuknetinfrastructure.external.messaging.outbox"
})
@EnableElasticsearchRepositories(basePackages = "org.example.deuknetinfrastructure.external.search")
@EnableJpaAuditing
public class DeuknetApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeuknetApplication.class, args);
    }

}
