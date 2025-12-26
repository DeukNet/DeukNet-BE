package org.example.deuknetinfrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.JwtPort;
import org.example.deuknetpresentation.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtPort jwtPort;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtPort jwtPort, UserRepository userRepository) {
        this.jwtPort = jwtPort;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No valid Authorization header found for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            UUID userId = jwtPort.validateToken(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // User 조회하여 role 기반 권한 설정
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                userRepository.findById(userId).ifPresent(user -> {
                    authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
                    log.debug("User authenticated - role: {}, id: {}", user.getRole().name(), userId);
                });

                UserPrincipal principal = new UserPrincipal(userId);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // JWT 검증 실패 - SecurityContext를 설정하지 않고 필터 체인 계속 진행
            // Spring Security가 해당 엔드포인트의 권한 요구사항에 따라 처리
            // - permitAll(): 인증 없이 통과
            // - authenticated(): 401 Unauthorized 자동 반환
            log.debug("JWT validation failed for URI: {}, Error: {}", requestURI, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
