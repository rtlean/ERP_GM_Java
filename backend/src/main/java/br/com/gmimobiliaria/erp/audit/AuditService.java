package br.com.gmimobiliaria.erp.audit;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/**
 * Grava em {@code audit_events}, a mesma tabela do sistema atual, para que a auditoria continue em um lugar só.
 * Os eventos do novo sistema levam {@code metadata.origin = "erp-gm-java"} para distinguir a origem.
 */
@Service
public class AuditService {
    private static final String ORIGIN_METADATA = "{\"origin\":\"erp-gm-java\"}";

    private final JdbcClient jdbc;
    private final Clock clock;

    public AuditService(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    public void record(long userId, Long companyId, String action, String entity, Long entityId) {
        jdbc.sql("""
                        INSERT INTO audit_events (user_id, company_id, action, entity, entity_id, metadata, created_at)
                        VALUES (:userId, :companyId, :action, :entity, :entityId, :metadata, :createdAt)
                        """)
                .param("userId", userId)
                .param("companyId", companyId)
                .param("action", action)
                .param("entity", entity)
                .param("entityId", entityId)
                .param("metadata", ORIGIN_METADATA)
                .param("createdAt", OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC))
                .update();
    }
}
