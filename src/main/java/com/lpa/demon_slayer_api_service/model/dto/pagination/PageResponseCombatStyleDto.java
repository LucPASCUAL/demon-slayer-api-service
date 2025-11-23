package com.lpa.demon_slayer_api_service.model.dto.pagination;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;

import java.util.List;

/**
 *
 * @param pagination   The JSON Object key in the body of the HTTP response that contains a {@link PaginationDto}
 * @param content      The JSON Array key in the body of the HTTP response that contains a List of {@link CombatStyleDto}
 */
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson will ignore any additional fields present in the JSON
public record PageResponseCombatStyleDto(
        PaginationDto pagination,
        List<CombatStyleDto> content
) {}
