package com.wasac.billing.serviceImpl;

import com.wasac.billing.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String fullName, String otp) {
        String message = "Dear " + fullName + ",\n\n"
                + "Your Utility Billing System account verification OTP is: " + otp + "\n"
                + "This OTP expires in 10 minutes.\n\n"
                + "Regards,\nWASAC/REG Utility Billing Team";
        sendNotificationEmail(to, "Utility Billing Account Verification OTP", message);
    }

    @Override
    public void sendNotificationEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(message);
            mailSender.send(mail);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
