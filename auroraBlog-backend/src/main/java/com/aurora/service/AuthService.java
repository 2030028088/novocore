package com.aurora.service;

import com.aurora.entity.User;
import com.aurora.mapper.UserMapper;
import com.aurora.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final ConcurrentHashMap<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    public static class VerificationCode {
        public String code;
        public String email;
        public LocalDateTime expireTime;
        public String type;

        public VerificationCode(String code, String email, LocalDateTime expireTime, String type) {
            this.code = code;
            this.email = email;
            this.expireTime = expireTime;
            this.type = type;
        }
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    public Map<String, Object> sendVerificationCode(String email, String type) {
        Map<String, Object> result = new HashMap<>();

        User existingUser = userMapper.findByEmail(email);
        if ("register".equals(type) && existingUser != null && Boolean.TRUE.equals(existingUser.getEmailVerified())) {
            result.put("success", false);
            result.put("message", "邮箱已被注册");
            return result;
        }

        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        verificationCodes.put(email, new VerificationCode(code, email, expireTime, type));

        emailService.sendVerificationCodeEmail(email, code);

        result.put("success", true);
        result.put("message", "验证码已发送到您的邮箱");
        
        return result;
    }

    public Map<String, Object> verifyCode(String email, String code, String type) {
        Map<String, Object> result = new HashMap<>();

        VerificationCode verificationCode = verificationCodes.get(email);
        
        if (verificationCode == null) {
            result.put("success", false);
            result.put("message", "请先获取验证码");
            return result;
        }

        if (!verificationCode.type.equals(type)) {
            result.put("success", false);
            result.put("message", "验证码类型不匹配");
            return result;
        }

        if (verificationCode.expireTime.isBefore(LocalDateTime.now())) {
            verificationCodes.remove(email);
            result.put("success", false);
            result.put("message", "验证码已过期");
            return result;
        }

        if (!verificationCode.code.equals(code)) {
            result.put("success", false);
            result.put("message", "验证码错误");
            return result;
        }

        verificationCodes.remove(email);
        
        result.put("success", true);
        result.put("message", "验证成功");
        
        return result;
    }

    public Map<String, Object> login(String email, String password) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            result.put("success", false);
            result.put("message", "邮箱未验证");
            return result;
        }

        String token = jwtUtil.generateToken(user.getEmail());
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername() != null ? user.getUsername() : email.split("@")[0]);
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        
        result.put("success", true);
        result.put("token", token);
        result.put("user", userMap);
        
        return result;
    }

    public Map<String, Object> register(String username, String email, String password, String code) {
        Map<String, Object> result = new HashMap<>();

        VerificationCode verificationCode = verificationCodes.get(email);
        
        if (verificationCode == null) {
            result.put("success", false);
            result.put("message", "请先获取验证码");
            return result;
        }

        if (!verificationCode.type.equals("register")) {
            result.put("success", false);
            result.put("message", "验证码类型不匹配");
            return result;
        }

        if (verificationCode.expireTime.isBefore(LocalDateTime.now())) {
            verificationCodes.remove(email);
            result.put("success", false);
            result.put("message", "验证码已过期");
            return result;
        }

        if (!verificationCode.code.equals(code)) {
            result.put("success", false);
            result.put("message", "验证码错误");
            return result;
        }

        if (userMapper.findByEmail(email) != null) {
            verificationCodes.remove(email);
            result.put("success", false);
            result.put("message", "邮箱已被注册");
            return result;
        }

        if (userMapper.findByUsername(username) != null) {
            verificationCodes.remove(email);
            result.put("success", false);
            result.put("message", "用户名已被使用");
            return result;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEmailVerified(true);

        userMapper.insert(user);

        verificationCodes.remove(email);

        result.put("success", true);
        result.put("message", "注册成功");
        
        return result;
    }

    public Map<String, Object> verifyEmail(String token) {
        Map<String, Object> result = new HashMap<>();

        User user = userMapper.findByVerificationToken(token);
        if (user == null) {
            result.put("success", false);
            result.put("message", "无效的验证链接");
            return result;
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userMapper.updateById(user);

        result.put("success", true);
        result.put("message", "邮箱验证成功");

        return result;
    }

    public Map<String, Object> verifyUserEmail(String email) {
        Map<String, Object> result = new HashMap<>();

        User user = userMapper.findByEmail(email);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        user.setEmailVerified(true);
        userMapper.updateById(user);

        result.put("success", true);
        result.put("message", "邮箱验证成功");

        return result;
    }

    public Map<String, Object> forgotPassword(String email) {
        Map<String, Object> result = new HashMap<>();

        User user = userMapper.findByEmail(email);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        verificationCodes.put(email, new VerificationCode(code, email, expireTime, "reset-password"));

        emailService.sendPasswordResetCodeEmail(email, code);

        result.put("success", true);
        result.put("message", "验证码已发送到您的邮箱");
        
        return result;
    }

    public Map<String, Object> resetPassword(String email, String code, String newPassword) {
        Map<String, Object> result = new HashMap<>();

        VerificationCode verificationCode = verificationCodes.get(email);
        
        if (verificationCode == null) {
            result.put("success", false);
            result.put("message", "请先获取验证码");
            return result;
        }

        if (!verificationCode.type.equals("reset-password")) {
            result.put("success", false);
            result.put("message", "验证码类型不匹配");
            return result;
        }

        if (verificationCode.expireTime.isBefore(LocalDateTime.now())) {
            verificationCodes.remove(email);
            result.put("success", false);
            result.put("message", "验证码已过期");
            return result;
        }

        if (!verificationCode.code.equals(code)) {
            result.put("success", false);
            result.put("message", "验证码错误");
            return result;
        }

        User user = userMapper.findByEmail(email);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        verificationCodes.remove(email);

        result.put("success", true);
        result.put("message", "密码重置成功");
        
        return result;
    }

    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public String getUserEmailFromToken(String token) {
        try {
            return jwtUtil.extractEmail(token);
        } catch (Exception e) {
            return null;
        }
    }
}