package com.twentyfive.twentyfivedb.fidelity.service;

import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.AuditLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Registro delle operazioni effettuate su carte, gruppi card e contatti.
 * I log si auto-eliminano dopo RETENTION_DAYS giorni tramite indice TTL su createdAt.
 */
@Service
public class AuditLogService {

    /** Giorni di conservazione dei log prima dell'auto-eliminazione */
    private static final long RETENTION_DAYS = 90;

    public static final String ENTITY_CARD = "CARD";
    public static final String ENTITY_CARD_GROUP = "CARD_GROUP";
    public static final String ENTITY_CONTACT = "CONTACT";

    public static final String OP_CREATE = "CREATE";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_DELETE = "DELETE";
    public static final String OP_ACTIVATE = "ACTIVATE";
    public static final String OP_DEACTIVATE = "DEACTIVATE";
    public static final String OP_CHANGE_GROUP = "CHANGE_GROUP";
    public static final String OP_RESET_SCANS = "RESET_SCANS";
    public static final String OP_SCAN = "SCAN";
    public static final String OP_ADD_POINTS = "ADD_POINTS";
    public static final String OP_REMOVE_POINTS = "REMOVE_POINTS";
    public static final String OP_CLAIM_PRIZE = "CLAIM_PRIZE";

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

    /**
     * Registro dell'utente, dalle operazioni più recenti. Tutti i filtri
     * (tipo entità, operazione, periodo, ricerca nei dettagli) sono opzionali.
     */
    public Page<AuditLog> getLogs(String ownerId, String entityType, String operation,
                                  LocalDate fromDate, LocalDate toDate, String searchText,
                                  int page, int size) {

        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("ownerId").is(ownerId));

        if (entityType != null && !entityType.isBlank()) {
            filters.add(Criteria.where("entityType").is(entityType));
        }

        if (operation != null && !operation.isBlank()) {
            filters.add(Criteria.where("operation").is(operation));
        }

        if (fromDate != null) {
            filters.add(Criteria.where("createdAt").gte(asDate(fromDate.atStartOfDay())));
        }

        if (toDate != null) {
            filters.add(Criteria.where("createdAt").lte(asDate(toDate.atTime(LocalTime.MAX))));
        }

        if (searchText != null && !searchText.isBlank()) {
            // Ogni parola deve comparire nei dettagli (codice card, nome, cognome, ...):
            // così "Rossi Mario" trova anche "Mario Rossi", indipendentemente dall'ordine.
            for (String token : searchText.trim().split("\\s+")) {
                if (!token.isBlank()) {
                    filters.add(Criteria.where("details").regex(Pattern.quote(token), "i"));
                }
            }
        }

        Criteria criteria = new Criteria().andOperator(filters.toArray(new Criteria[0]));

        // Il totale va calcolato senza paginazione: due query sullo stesso criterio
        long total = mongoTemplate.count(Query.query(criteria), AuditLog.class);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AuditLog> logs = mongoTemplate.find(Query.query(criteria).with(pageable), AuditLog.class);

        return new PageImpl<>(logs, pageable, total);
    }

    private Date asDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
