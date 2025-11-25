package com.lpa.demon_slayer_api_service.model.dto.character;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
  *
  * @param content the JSON Array key in the body of HTTP response that contains a {@link CharacterDto}
  */
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson will ignore any additional fields present in the JSON
public record CharacterResponseDto(
        List<CharacterDto> content
) {}
