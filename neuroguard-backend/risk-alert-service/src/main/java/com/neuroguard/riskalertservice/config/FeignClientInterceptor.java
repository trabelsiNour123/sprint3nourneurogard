package com.neuroguard.riskalertservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        String authHeader = null;

        // First try to get from servlet request context (for HTTP requests)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            authHeader = request.getHeader(AUTHORIZATION_HEADER);
        }

        // If not found in request context, try to get from SecurityContext (for async/scheduled tasks)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() instanceof String) {
                String token = (String) auth.getCredentials();
                if (token != null && !token.isEmpty()) {
                    authHeader = "Bearer " + token;
                }
            }
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            template.header(AUTHORIZATION_HEADER, authHeader);
        }
    }
}