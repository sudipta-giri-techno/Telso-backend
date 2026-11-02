package com.example.app.service;

public interface EmailService {
    void sendPasswordResetOtp(String toEmail, String otp, int expiryMinutes);
}