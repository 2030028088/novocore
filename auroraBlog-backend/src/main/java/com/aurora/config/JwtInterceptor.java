package com.aurora.config;

import com.aurora.entity.ApiResponse;
import com.aurora.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/send-code",
            "/api/auth/verify-code",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/github/",
            "/oauth2/authorization/",
            "/login/oauth2/code/",
            "/api/articles",
            "/api/articles/",
            "/api/projects",
            "/api/downloads",
            "/api/images/"
    );

    private static final List<String> ADMIN_PATHS = List.of(
            "/api/admin/",
            "/api/articles/",
            "/api/projects/",
            "/api/downloads/",
            "/api/images/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();

        if (isPublicPath(requestUri)) {
            return true;
        }

        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Authorization token missing");
            return false;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            sendUnauthorizedResponse(response, "Invalid or expired token");
            return false;
        }

        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        request.setAttribute("userId", email);
        request.setAttribute("userRole", role);

        if (requiresAdminRole(requestUri) && !"ADMIN".equals(role)) {
            sendForbiddenResponse(response, "Admin role required");
            return false;
        }

        return true;
    }

    private boolean isPublicPath(String uri) {
        for (String path : PUBLIC_PATHS) {
            if (uri.startsWith(path) || uri.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresAdminRole(String uri) {
        for (String path : ADMIN_PATHS) {
            if (uri.startsWith(path)) {
                if (uri.startsWith("/api/articles/") && (uri.contains("/favorite") || uri.contains("/like"))) {
                    return false;
                }
                if (uri.startsWith("/api/images/") && !uri.contains("upload")) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.unauthorized(message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.forbidden(message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}