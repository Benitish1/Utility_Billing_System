package com.wasac.billing.service;

public interface EmailService {
    void sendOtpEmail(String to, String fullName, String otp);
    void sendNotificationEmail(String to, String subject, String message);
}
