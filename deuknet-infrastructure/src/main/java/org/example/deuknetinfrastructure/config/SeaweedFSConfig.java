package org.example.deuknetinfrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SeaweedFS 설정
 */
@Configuration
public class SeaweedFSConfig {

    @Value("${seaweedfs.master-url:http://localhost:9333}")
    private String masterUrl;

    @Value("${seaweedfs.external-url:http://localhost:8090}")
    private String externalUrl;

    @Bean("seaweedFSMasterUrl")
    public String seaweedFSMasterUrl() {
        return masterUrl;
    }

    @Bean("seaweedFSExternalUrl")
    public String seaweedFSExternalUrl() {
        return externalUrl;
    }
}
