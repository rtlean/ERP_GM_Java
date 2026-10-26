package br.com.gmimobiliaria.erp.cadastros.tenants;

import br.com.gmimobiliaria.erp.common.DocumentMask;
import br.com.gmimobiliaria.erp.common.ListQuery;
import br.com.gmimobiliaria.erp.common.PageResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Locatários ({@code tenants}). Somente leitura nesta fase. */
@Repository
public class TenantRepository {
    private final JdbcClient jdbc;

    public TenantRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public PageResult<TenantSummary> list(long companyId, ListQuery query) {
        var where = " WHERE t.company_id = :companyId"
                + (query.hasSearch()
                    ? " AND (lower(t.full_name) LIKE :pattern OR lower(coalesce(t.email, '')) LIKE :pattern)"
                    : "");

        var count = jdbc.sql("SELECT count(*) FROM tenants t" + where).param("companyId", companyId);
        var select = jdbc.sql("""
                        SELECT t.id, t.full_name, t.cpf, t.email, t.phone, t.rent_cents, t.property_type,
                               t.owner_id, o.full_name AS owner_name
                          FROM tenants t
                          LEFT JOIN owners o ON o.id = t.owner_id AND o.company_id = t.company_id
                        """ + where + " ORDER BY t.full_name, t.id LIMIT :size OFFSET :offset")
                .param("companyId", companyId)
                .param("size", query.size())
                .param("offset", query.offset());
        if (query.hasSearch()) {
            count = count.param("pattern", query.likePattern());
            select = select.param("pattern", query.likePattern());
        }

        var rows = select.query((rs, row) -> new TenantRow(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("cpf"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getLong("rent_cents"),
                rs.getString("property_type"),
                rs.getObject("owner_id", Long.class),
                rs.getString("owner_name"))).list();

        var links = activeContractOwners(companyId, rows.stream().map(TenantRow::id).toList());
        var items = rows.stream().map(row -> row.toSummary(links.getOrDefault(row.id(), List.of()))).toList();
        return new PageResult<>(items, query.page(), query.size(), count.query(Long.class).single());
    }

    /** Locadores dos imóveis com contrato ativo de cada Locatário (um Locatário pode alugar de vários Locadores). */
    private Map<Long, List<OwnerRef>> activeContractOwners(long companyId, List<Long> tenantIds) {
        if (tenantIds.isEmpty()) {
            return Map.of();
        }
        var result = new LinkedHashMap<Long, List<OwnerRef>>();
        jdbc.sql("""
                        SELECT c.tenant_id, p.owner_id, o.full_name
                          FROM contracts c
                          JOIN properties p ON p.id = c.property_id
                          JOIN owners o ON o.id = p.owner_id
                         WHERE c.company_id = :companyId AND p.company_id = :companyId
                           AND CAST(c.status AS VARCHAR) = 'active'
                           AND c.tenant_id IN (:tenantIds)
                         ORDER BY c.tenant_id, c.id
                        """)
                .param("companyId", companyId)
                .param("tenantIds", tenantIds)
                .query(rs -> {
                    result.computeIfAbsent(rs.getLong("tenant_id"), id -> new ArrayList<>())
                            .add(new OwnerRef(rs.getLong("owner_id"), rs.getString("full_name")));
                });
        return result;
    }

    private record OwnerRef(long id, String name) {
    }

    private record TenantRow(long id, String fullName, String cpf, String email, String phone, long rentCents,
                             String propertyType, Long ownerId, String ownerName) {

        /** Mesma regra de {@code tenantOwnerSet}: Locador principal do cadastro + Locadores dos contratos, sem repetir. */
        TenantSummary toSummary(List<OwnerRef> contractOwners) {
            var owners = new LinkedHashMap<Long, String>();
            if (ownerId != null && ownerId > 0 && ownerName != null) {
                owners.put(ownerId, ownerName);
            }
            contractOwners.forEach(owner -> owners.putIfAbsent(owner.id(), owner.name()));
            return new TenantSummary(id, fullName, DocumentMask.mask(cpf), email, phone, rentCents, propertyType,
                    List.copyOf(owners.values()));
        }
    }

    public record TenantSummary(long id, String fullName, String documentMasked, String email, String phone,
                                long rentCents, String propertyType, List<String> ownerNames) {
    }
}
