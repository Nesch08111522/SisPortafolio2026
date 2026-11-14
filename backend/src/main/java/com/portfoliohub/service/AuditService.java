package com.portfoliohub.service;

import com.portfoliohub.domain.AuditLog;
import com.portfoliohub.repo.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogs;

    public AuditService(AuditLogRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    public void record(UUID actorId, String action, String entityType, String entityId, String details, String ip) {
        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIp(ip);
        auditLogs.save(log);
    }
}
