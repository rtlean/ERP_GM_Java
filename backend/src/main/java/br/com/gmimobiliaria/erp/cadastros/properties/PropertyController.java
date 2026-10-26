package br.com.gmimobiliaria.erp.cadastros.properties;

import br.com.gmimobiliaria.erp.cadastros.properties.PropertyRepository.PropertySummary;
import br.com.gmimobiliaria.erp.common.ApiException;
import br.com.gmimobiliaria.erp.common.ListQuery;
import br.com.gmimobiliaria.erp.common.PageResult;
import br.com.gmimobiliaria.erp.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {
    private final PropertyRepository properties;

    public PropertyController(PropertyRepository properties) {
        this.properties = properties;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROPERTIES_READ')")
    public PageResult<PropertySummary> list(@AuthenticationPrincipal CurrentUser user,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size) {
        var occupancy = status == null || status.isBlank() ? null : status;
        if (occupancy != null && !PropertyRepository.OCCUPANCY_STATUSES.contains(occupancy)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Situação do imóvel inválida.");
        }
        return properties.list(user.companyId(), ListQuery.of(q, page, size), occupancy);
    }
}
