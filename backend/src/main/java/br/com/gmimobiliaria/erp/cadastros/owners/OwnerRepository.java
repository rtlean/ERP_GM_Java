package br.com.gmimobiliaria.erp.cadastros.owners;

import br.com.gmimobiliaria.erp.common.DocumentMask;
import br.com.gmimobiliaria.erp.common.ListQuery;
import br.com.gmimobiliaria.erp.common.PageResult;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Locadores ({@code owners}). Somente leitura nesta fase. */
@Repository
public class OwnerRepository {
    private final JdbcClient jdbc;

    public OwnerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public PageResult<OwnerSummary> list(long companyId, ListQuery query) {
        var where = " WHERE o.company_id = :companyId"
                + (query.hasSearch()
                    ? " AND (lower(o.full_name) LIKE :pattern OR lower(coalesce(o.email, '')) LIKE :pattern)"
                    : "");

        var count = jdbc.sql("SELECT count(*) FROM owners o" + where)
                .param("companyId", companyId);
        var select = jdbc.sql("""
                        SELECT o.id, o.full_name, o.document, o.email, o.phone, o.preferred_repass_method,
                               (SELECT count(*) FROM properties p
                                 WHERE p.owner_id = o.id AND p.company_id = o.company_id) AS properties_count
                          FROM owners o
                        """ + where + " ORDER BY o.full_name, o.id LIMIT :size OFFSET :offset")
                .param("companyId", companyId)
                .param("size", query.size())
                .param("offset", query.offset());
        if (query.hasSearch()) {
            count = count.param("pattern", query.likePattern());
            select = select.param("pattern", query.likePattern());
        }

        var items = select.query((rs, row) -> new OwnerSummary(
                rs.getLong("id"),
                rs.getString("full_name"),
                DocumentMask.mask(rs.getString("document")),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("preferred_repass_method"),
                rs.getInt("properties_count"))).list();
        return new PageResult<>(items, query.page(), query.size(), count.query(Long.class).single());
    }

    public record OwnerSummary(long id, String fullName, String documentMasked, String email, String phone,
                               String preferredRepassMethod, int propertiesCount) {
    }
}
