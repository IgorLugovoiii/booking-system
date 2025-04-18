package com.example.notification_service.services;

import com.example.notification_service.dtos.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Transactional
    public void sendNotification(NotificationRequest notification){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getTo());
        message.setSubject(notification.getSubject());
        message.setText(notification.getMessage());

        mailSender.send(message);
    }
}
