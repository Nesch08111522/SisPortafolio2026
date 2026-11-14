package com.portfoliohub.web;

import com.portfoliohub.repo.OutboundMessageRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "test"})
@RequestMapping("/api/v1/dev/mailbox")
public class DevMailboxController {

    private final OutboundMessageRepository messages;

    public DevMailboxController(OutboundMessageRepository messages) {
        this.messages = messages;
    }

    @GetMapping
    public Object mailbox(@RequestParam String recipient) {
        return messages.findTop20ByRecipientIgnoreCaseOrderByCreatedAtDesc(recipient);
    }
}
