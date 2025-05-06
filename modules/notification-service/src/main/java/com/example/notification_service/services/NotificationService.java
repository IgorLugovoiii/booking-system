package com.example.notification_service.services;

import com.example.notification_service.dtos.NotificationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Service
public class NotificationService {
    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Transactional
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendNotificationFallback")
    @Retry(name = "notificationService")
    @RateLimiter(name = "notificationService")
    public void sendNotification(NotificationRequest notification){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getTo());
        message.setSubject(notification.getSubject());
        message.setText(notification.getMessage());

        mailSender.send(message);
    }

    public void sendNotificationFallback(NotificationRequest notification, Throwable t) {
        logger.severe("Error sending notification to " + notification.getTo() + ": " + t.getMessage());
        throw new IllegalStateException("Fallback: can't send notification to " + notification.getTo());
    }
}
