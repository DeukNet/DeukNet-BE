package org.example.deuknetinfrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.deuknetapplication.port.out.security.JwtPort;
import org.example.deuknetpresentation.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtPort jwtPort;

    public JwtAuthenticationFilter(JwtPort jwtPort) {
        this.jwtPort = jwtPort;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.info("JWT Filter - URI: {}, Auth header present: {}", requestURI, authHeader != null);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No valid Authorization header found for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            UUID userId = jwtPort.validateToken(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("JWT validation successful, userId: {}", userId);
                UserPrincipal principal = new UserPrincipal(userId);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.info("JWT validation failed or authentication already set");
            }
        } catch (Exception e) {
            log.error("JWT validation error for URI: {}, Error: {}", requestURI, e.getMessage(), e);
            // 토큰 검증 실패 시 인증 정보 설정하지 않음
        }

        filterChain.doFilter(request, response);
    }
}
