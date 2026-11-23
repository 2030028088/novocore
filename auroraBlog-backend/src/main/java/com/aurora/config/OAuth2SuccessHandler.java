package com.aurora.config;

import com.aurora.entity.User;
import com.aurora.mapper.UserMapper;
import com.aurora.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String githubId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("login");
        String name = (String) attributes.get("name");

        logger.info("GitHub OAuth2 success - ID: {}, Email: {}, Username: {}", githubId, email, username);

        String userEmail = email != null && !email.isEmpty() ? email : "github_" + githubId + "@example.com";
        String displayName = username != null && !username.isEmpty() ? username : name;

        User existingUser = userMapper.findByEmail(userEmail);

        if (existingUser == null) {
            User newUser = new User();
            newUser.setUsername(displayName);
            newUser.setEmail(userEmail);
            newUser.setPassword("");
            newUser.setRole("USER");
            newUser.setEmailVerified(true);
            userMapper.insert(newUser);
            existingUser = newUser;
            logger.info("Created new user from GitHub: {}", userEmail);
        }

        String token = jwtUtil.generateToken(existingUser.getEmail(), existingUser.getRole());

        String redirectUrl = frontendUrl + "/login?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        
        logger.info("Redirecting to frontend with token: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}
