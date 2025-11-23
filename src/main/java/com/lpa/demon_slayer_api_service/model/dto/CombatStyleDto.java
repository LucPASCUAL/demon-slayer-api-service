package com.lpa.demon_slayer_api_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lpa.demon_slayer_api_service.model.Identifiable;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

/**
  *
  * @param id       the combat style ID from API side
  * @param name     the combat style name (Sun breathing, Water breathing...)
  * @param description      the combat description
  */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson will ignore any additional fields present in the JSON
public record CombatStyleDto(
        Long id,
        String name,
        String description
) implements Identifiable {

    @Override
    @NotNull
    public String toString() {
        return """
               CombatStyleDto{
                    id=%d,
                    name="%s",
                    description="%s",
                }
               """.formatted(id, name,description);
    }
}
