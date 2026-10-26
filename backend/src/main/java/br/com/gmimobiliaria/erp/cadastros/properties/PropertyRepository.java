package br.com.gmimobiliaria.erp.cadastros.properties;

import br.com.gmimobiliaria.erp.common.ListQuery;
import br.com.gmimobiliaria.erp.common.PageResult;
import java.util.Set;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Imóveis ({@code properties}). Somente leitura nesta fase. */
@Repository
public class PropertyRepository {
    /** Valores do enum {@code occupancy_status} do banco atual. */
    public static final Set<String> OCCUPANCY_STATUSES = Set.of("occupied", "vacant", "maintenance");

    private final JdbcClient jdbc;

    public PropertyRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public PageResult<PropertySummary> list(long companyId, ListQuery query, String occupancyStatus) {
        var where = new StringBuilder(" WHERE p.company_id = :companyId");
        if (query.hasSearch()) {
            where.append(" AND (lower(p.code) LIKE :pattern OR lower(p.address) LIKE :pattern")
                    .append(" OR lower(p.city) LIKE :pattern OR lower(coalesce(o.full_name, '')) LIKE :pattern)");
        }
        if (occupancyStatus != null) {
            // CAST: o parâmetro chega como texto e a coluna é um enum do Postgres.
            where.append(" AND CAST(p.occupancy_status AS VARCHAR) = :status");
        }
        var from = " FROM properties p LEFT JOIN owners o ON o.id = p.owner_id AND o.company_id = p.company_id";

        var count = jdbc.sql("SELECT count(*)" + from + where).param("companyId", companyId);
        // Locatário do contrato ativo, como na lista do sistema atual; o mais recente se houver mais de um.
        var select = jdbc.sql("""
                        SELECT p.id, p.code, p.type, p.address, p.city, p.state, p.zip_code, p.rent_cents,
                               CAST(p.occupancy_status AS VARCHAR) AS occupancy_status,
                               p.owner_id, o.full_name AS owner_name,
                               (SELECT t.full_name FROM contracts c JOIN tenants t ON t.id = c.tenant_id
                                 WHERE c.property_id = p.id AND c.company_id = p.company_id
                                   AND CAST(c.status AS VARCHAR) = 'active'
                                 ORDER BY c.start_date DESC, c.id DESC
                                 FETCH FIRST 1 ROWS ONLY) AS tenant_name
                        """ + from + where + " ORDER BY p.code, p.id LIMIT :size OFFSET :offset")
                .param("companyId", companyId)
                .param("size", query.size())
                .param("offset", query.offset());
        if (query.hasSearch()) {
            count = count.param("pattern", query.likePattern());
            select = select.param("pattern", query.likePattern());
        }
        if (occupancyStatus != null) {
            count = count.param("status", occupancyStatus);
            select = select.param("status", occupancyStatus);
        }

        var items = select.query((rs, row) -> new PropertySummary(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("type"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("zip_code"),
                rs.getLong("rent_cents"),
                rs.getString("occupancy_status"),
                rs.getObject("owner_id", Long.class),
                rs.getString("owner_name"),
                rs.getString("tenant_name"))).list();
        return new PageResult<>(items, query.page(), query.size(), count.query(Long.class).single());
    }

    public record PropertySummary(long id, String code, String type, String address, String city, String state,
                                  String zipCode, long rentCents, String occupancyStatus, Long ownerId,
                                  String ownerName, String tenantName) {
    }
}
