package com.aurora.controller;

import com.aurora.entity.User;
import com.aurora.mapper.UserMapper;
import com.aurora.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class OAuthController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/github/url")
    public ResponseEntity<Map<String, Object>> getGitHubAuthUrl(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String backendUrl;
        if (serverPort == 80 || serverPort == 443) {
            backendUrl = scheme + "://" + serverName;
        } else {
            backendUrl = scheme + "://" + serverName + ":" + serverPort;
        }
        String authUrl = backendUrl + "/oauth2/authorization/github";
        result.put("success", true);
        result.put("redirectUrl", authUrl);
        logger.info("GitHub OAuth2 auth URL: {}", authUrl);
        return ResponseEntity.ok(result);
    }
}
