package com.twentyfive.twentyfivedb.fidelity.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Log di audit (solo DB, non esposto al frontend) per le operazioni di
 * creazione/eliminazione di carte e gruppi card.
 * I documenti si auto-eliminano tramite indice TTL (vedi AuditLogService).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("fidelity_audit_log")
public class AuditLog {

    @Id
    private String id;
    private String entityType;   // CARD | CARD_GROUP
    private String operation;    // CREATE | DELETE
    private String entityId;
    private String ownerId;
    private String details;
    private Date createdAt;
}
