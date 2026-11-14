package com.portfoliohub.repo;

import com.portfoliohub.domain.OutboundMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboundMessageRepository extends JpaRepository<OutboundMessage, UUID> {
    List<OutboundMessage> findTop20ByRecipientIgnoreCaseOrderByCreatedAtDesc(String recipient);
}
