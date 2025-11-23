package com.lpa.demon_slayer_api_service.model.dto.character;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lpa.demon_slayer_api_service.model.Identifiable;
import com.lpa.demon_slayer_api_service.service.DemonSlayerApiService;
import org.jetbrains.annotations.NotNull;

/**
  * Used when retrieving all characters from the Demon Slayer public API in the {@link DemonSlayerApiService} class
  *
  * @param id       the character ID on the API side
  * @param name     the character's first and last name
  * @param gender   the character gender (Male, Female...)
  * @param race     the character race (Human, Demon...)
  * @param description  the character description
  * @param img  the URL of the character image
  */
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson will ignore any additional fields present in the JSON
public record CharacterSummaryDto(
        Long id,
        String name,
        String gender,
        String race,
        String description,
        String img
) implements Identifiable {

    @Override
    @NotNull
    public String toString() {
        return """
               CharacterSummaryDto{
                    id=%d,
                    name="%s",
                    gender="%s",
                    race="%s",
                    description="%s",
                    img="%s"
                }
               """.formatted(id, name, gender, race, description, img);
    }
}
