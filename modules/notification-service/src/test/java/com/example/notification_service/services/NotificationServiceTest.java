package com.example.notification_service.services;

import com.example.notification_service.dtos.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testSendNotification_shouldSendEmail() {
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .message("Hello from test!")
                .build();

        notificationService.sendNotification(notificationRequest);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getTo());
        assertEquals("test@example.com", sentMessage.getTo()[0]);
        assertEquals("Test Subject", sentMessage.getSubject());
        assertEquals("Hello from test!", sentMessage.getText());
    }

    @Test
    void testSendNotification_shouldHandleException() {
        NotificationRequest request = NotificationRequest.builder()
                .to("fail@example.com")
                .subject("Fails")
                .message("Expect exception")
                .build();

        doThrow(new RuntimeException("Mail server error")).when(javaMailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> notificationService.sendNotification(request));

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
