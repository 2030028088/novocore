package com.aurora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = "http://localhost:5173/verify-email?token=" + token;
        String content = "欢迎注册 Aurora Blog！\n\n请点击以下链接验证您的邮箱：\n" + verificationUrl + "\n\n如果这不是您的操作，请忽略此邮件。";
        
        if (emailEnabled && isEmailConfigured()) {
            sendEmail(toEmail, "Aurora Blog - 邮箱验证", content);
        } else {
            logger.info("[DEV MODE] 邮箱验证链接: {}", verificationUrl);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        String content = "您请求重置 Aurora Blog 账户密码。\n\n请点击以下链接重置密码：\n" + resetUrl + "\n\n如果这不是您的操作，请忽略此邮件。";
        
        if (emailEnabled && isEmailConfigured()) {
            sendEmail(toEmail, "Aurora Blog - 密码重置", content);
        } else {
            logger.info("[DEV MODE] 密码重置链接: {}", resetUrl);
        }
    }
    
    public void sendVerificationCodeEmail(String toEmail, String code) {
        String content = "您的邮箱验证验证码是：" + code + "\n\n验证码有效期为5分钟，请尽快使用。\n\n如果这不是您的操作，请忽略此邮件。";
        
        if (emailEnabled && isEmailConfigured() && mailSender != null) {
            try {
                sendEmail(toEmail, "Aurora Blog - 邮箱验证码", content);
            } catch (Exception e) {
                logger.warn("邮件发送失败，切换到开发模式: {}", e.getMessage());
                logger.info("[DEV MODE] 验证码已发送到邮箱 {}：{}", toEmail, code);
            }
        } else {
            logger.info("[DEV MODE] 验证码已发送到邮箱 {}：{}", toEmail, code);
        }
    }
    
    public void sendPasswordResetCodeEmail(String toEmail, String code) {
        String content = "您的密码重置验证码是：" + code + "\n\n验证码有效期为5分钟，请尽快使用。\n\n如果这不是您的操作，请忽略此邮件。";
        
        if (emailEnabled && isEmailConfigured() && mailSender != null) {
            try {
                sendEmail(toEmail, "Aurora Blog - 密码重置验证码", content);
            } catch (Exception e) {
                logger.warn("邮件发送失败，切换到开发模式: {}", e.getMessage());
                logger.info("[DEV MODE] 密码重置验证码已发送到邮箱 {}：{}", toEmail, code);
            }
        } else {
            logger.info("[DEV MODE] 密码重置验证码已发送到邮箱 {}：{}", toEmail, code);
        }
    }
    
    private void sendEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail.isEmpty() ? "2030028088@qq.com" : fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            logger.info("邮件已发送到: {}", toEmail);
        } catch (Exception e) {
            logger.error("发送邮件失败 - 邮箱: {}, 错误: {}", toEmail, e.getMessage());
            logger.error("邮件发送失败详细信息:", e);
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
    }
    
    private boolean isEmailConfigured() {
        return fromEmail != null && !fromEmail.isEmpty();
    }
}