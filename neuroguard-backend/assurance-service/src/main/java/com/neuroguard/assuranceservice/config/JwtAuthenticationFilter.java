package com.neuroguard.assuranceservice.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("[JwtFilter] No Bearer token on request: {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtils.validateJwtToken(token)) {
                log.warn("[JwtFilter] Token validation failed for request: {} {}", request.getMethod(), request.getRequestURI());
                chain.doFilter(request, response);
                return;
            }

            DecodedJWT decodedJWT = jwtUtils.verifyToken(token);
            String username = decodedJWT.getSubject();
            String role = decodedJWT.getClaim("role").asString();
            Long userId = decodedJWT.getClaim("userId").asLong();

            log.debug("[JwtFilter] Authenticated user: {}, role: {}, userId: {}", username, role, userId);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            request.setAttribute("userId", userId);
            request.setAttribute("userRole", role);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception e) {
            log.error("[JwtFilter] JWT processing error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }

        chain.doFilter(request, response);
    }
}

