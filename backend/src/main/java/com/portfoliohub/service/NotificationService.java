package com.portfoliohub.service;

import com.portfoliohub.domain.Notification;
import com.portfoliohub.repo.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public void notify(UUID userId, String title, String body) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setBody(body);
        notifications.save(notification);
    }
}
