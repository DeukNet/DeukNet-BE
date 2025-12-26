package org.example.deuknetinfrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.example.deuknetdomain.domain.user.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                })
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
                .requestMatchers(HttpMethod.POST, "/api/categories").hasAuthority(Role.ADMIN.name())
                .requestMatchers(HttpMethod.PUT, "/api/categories/*").hasAuthority(Role.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE, "/api/categories/*").hasAuthority(Role.ADMIN.name())

                // User API (더 구체적인 패턴을 먼저 배치)
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/*").permitAll()

                // File API
                .requestMatchers(HttpMethod.POST, "/api/files/upload").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()

                // 기타 모든 요청 거부
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
