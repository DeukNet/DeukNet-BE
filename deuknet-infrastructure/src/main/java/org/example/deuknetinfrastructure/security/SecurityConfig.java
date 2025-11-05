package org.example.deuknetinfrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})  // Use WebMvcConfigurer CORS configuration
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // ========== 인증 불필요 ==========
                // Auth API
                .requestMatchers(HttpMethod.GET, "/api/auth/oauth/google").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/oauth/callback/google").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

                // Swagger UI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                // Actuator
                .requestMatchers("/actuator/health", "/api/health", "/").permitAll()

                // ========== 인증 필요 ==========
                // Post API - CUD (Create, Update, Delete)
                .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/posts/*/publish").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/*").authenticated()

                // Post API - Read (조회는 인증 불필요)
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/posts/*/view").permitAll()

                // Comment API
                .requestMatchers(HttpMethod.POST, "/api/posts/*/comments").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/*/comments/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/*/comments/*").authenticated()

                // Reaction API
                .requestMatchers(HttpMethod.POST, "/api/posts/*/reactions").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/*/reactions/*").authenticated()

                // Category API
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/categories").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/categories/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/categories/*").authenticated()

                // User API
                .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()

                // 기타 모든 요청 거부
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
