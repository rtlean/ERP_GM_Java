package br.com.gmimobiliaria.erp.cadastros.owners;

import br.com.gmimobiliaria.erp.cadastros.owners.OwnerRepository.OwnerSummary;
import br.com.gmimobiliaria.erp.common.ListQuery;
import br.com.gmimobiliaria.erp.common.PageResult;
import br.com.gmimobiliaria.erp.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owners")
public class OwnerController {
    private final OwnerRepository owners;

    public OwnerController(OwnerRepository owners) {
        this.owners = owners;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PEOPLE_READ')")
    public PageResult<OwnerSummary> list(@AuthenticationPrincipal CurrentUser user,
                                         @RequestParam(required = false) String q,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        return owners.list(user.companyId(), ListQuery.of(q, page, size));
    }
}
