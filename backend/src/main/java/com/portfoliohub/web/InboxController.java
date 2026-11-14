package com.portfoliohub.web;

import com.portfoliohub.domain.Notification;
import com.portfoliohub.repo.ContactMessageRepository;
import com.portfoliohub.repo.NotificationRepository;
import com.portfoliohub.security.CurrentUser;
import com.portfoliohub.service.PortfolioService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class InboxController {

    private final NotificationRepository notifications;
    private final ContactMessageRepository contacts;
    private final PortfolioService portfolios;

    public InboxController(NotificationRepository notifications, ContactMessageRepository contacts, PortfolioService portfolios) {
        this.notifications = notifications;
        this.contacts = contacts;
        this.portfolios = portfolios;
    }

    @GetMapping("/notifications")
    public Object notifications() {
        return notifications.findTop50ByUserIdOrderByCreatedAtDesc(CurrentUser.id());
    }

    @PostMapping("/notifications/{id}/read")
    @Transactional
    public void read(@PathVariable UUID id) {
        notifications.findById(id).filter(item -> item.getUserId().equals(CurrentUser.id())).ifPresent(item -> item.setReadAt(Instant.now()));
    }

    @GetMapping("/contacts")
    public Object contacts() {
        return contacts.findByPortfolioIdOrderByCreatedAtDesc(portfolios.owned(CurrentUser.id()).getId());
    }
}
