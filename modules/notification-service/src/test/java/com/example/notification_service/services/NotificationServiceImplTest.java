package com.example.notification_service.services;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.services.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {
    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private NotificationServiceImpl notificationServiceImpl;

    @Test
    void givenValidNotification_whenSendNotification_thenMailSent() {
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .message("Hello from test!")
                .build();

        notificationServiceImpl.sendNotification(notificationRequest);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly("test@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Test Subject");
        assertThat(sentMessage.getText()).isEqualTo("Hello from test!");
    }

    @Test
    void givenMailSenderThrowsException_whenSendNotification_thenExceptionPropagated() {
        NotificationRequest request = NotificationRequest.builder()
                .to("fail@example.com")
                .subject("Fails")
                .message("Expect exception")
                .build();

        doThrow(new RuntimeException("Mail server error"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> notificationServiceImpl.sendNotification(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mail server error");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
