package com.portfoliohub.service;

import com.portfoliohub.config.AppProperties;
import com.portfoliohub.domain.OutboundMessage;
import com.portfoliohub.repo.OutboundMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final OutboundMessageRepository messages;
    private final AppProperties properties;

    public MailService(ObjectProvider<JavaMailSender> mailSender, OutboundMessageRepository messages, AppProperties properties) {
        this.mailSender = mailSender;
        this.messages = messages;
        this.properties = properties;
    }

    public void send(String recipient, String subject, String body) {
        OutboundMessage message = new OutboundMessage();
        message.setRecipient(recipient);
        message.setSubject(subject);
        message.setBody(body);
        messages.save(message);
        if (properties.getMail().isExposeDevLink()) {
            log.info("Correo de desarrollo para {}: {}", recipient, subject);
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(properties.getMail().getFrom());
            mail.setTo(recipient);
            mail.setSubject(subject);
            mail.setText(body);
            sender.send(mail);
        } catch (RuntimeException exception) {
            log.warn("No se pudo entregar el correo a {}", recipient);
        }
    }
}
