package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.models.AuditLog;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Registra le operazioni di creazione/eliminazione di carte e gruppi su una
 * collezione dedicata (solo DB). I log si auto-eliminano dopo RETENTION_DAYS
 * giorni tramite indice TTL su createdAt.
 */
@Service
public class AuditLogService {

    /** Giorni di conservazione dei log prima dell'auto-eliminazione */
    private static final long RETENTION_DAYS = 90;

    public static final String ENTITY_CARD = "CARD";
    public static final String ENTITY_CARD_GROUP = "CARD_GROUP";
    public static final String OP_CREATE = "CREATE";
    public static final String OP_DELETE = "DELETE";

    private final MongoTemplate mongoTemplate;

    public AuditLogService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initTtlIndex() {
        // Indice TTL: MongoDB elimina i documenti RETENTION_DAYS giorni dopo createdAt
        mongoTemplate.indexOps(AuditLog.class)
                .ensureIndex(new Index().on("createdAt", Sort.Direction.ASC)
                        .expire(RETENTION_DAYS, TimeUnit.DAYS));
    }

    public void log(String entityType, String operation, String entityId, String ownerId, String details) {
        AuditLog entry = new AuditLog(null, entityType, operation, entityId, ownerId, details, new Date());
        mongoTemplate.save(entry);
    }
}
