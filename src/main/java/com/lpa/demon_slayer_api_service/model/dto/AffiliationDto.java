package com.lpa.demon_slayer_api_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

/**
  * @param name     the affiliation name (Demon Slayer, Hashira...)
  * @param description      the affiliation description
  */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson will ignore any additional fields present in the JSON
public record AffiliationDto(
        String name,
        String description
) {}
