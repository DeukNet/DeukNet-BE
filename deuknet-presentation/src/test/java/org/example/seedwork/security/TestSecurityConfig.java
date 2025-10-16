package org.example.seedwork.security;

import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

@TestConfiguration
public class TestSecurityConfig {

    public static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Bean
    @Primary
    public CurrentUserPort testCurrentUserPort() {
        return () -> TEST_USER_ID;
    }
}
