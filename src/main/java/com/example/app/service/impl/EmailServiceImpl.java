package com.example.app.service.impl;

import com.example.app.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@localhost}")
    private String from;

    @Override
    @Async
    public void sendPasswordResetOtp(String toEmail, String otp, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Your password reset code");
            helper.setText(buildBody(otp, expiryMinutes), true); // true = HTML

            mailSender.send(message);
            log.info("Password reset OTP email sent to {}", toEmail);
        } catch (Exception e) {
            // Don't rethrow: the API response must not reveal whether the email exists
            log.error("Failed to send OTP email to {}", toEmail, e);
        }
    }

    private String buildBody(String otp, int expiryMinutes) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto">
              <h2>Password reset</h2>
              <p>Use the code below to reset your password:</p>
              <p style="font-size:28px;font-weight:bold;letter-spacing:6px">%s</p>
              <p>This code expires in %d minutes. If you didn't request it, you can ignore this email.</p>
            </div>
            """.formatted(otp, expiryMinutes);
    }
}