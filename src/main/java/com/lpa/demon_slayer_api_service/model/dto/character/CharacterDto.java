package com.lpa.demon_slayer_api_service.model.dto.character;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lpa.demon_slayer_api_service.model.dto.AffiliationDto;
import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;
import lombok.Builder;
import com.lpa.demon_slayer_api_service.service.DemonSlayerApiService;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
  * Used when retrieving a single character from the Demon Slayer API in the {@link DemonSlayerApiService} class
  *
  * @param id       the character ID on the API side
  * @param name     the character's first and last name
  * @param gender   the character gender (Male, Female...)
  * @param race     the character race (Human, Demon...)
  * @param description  the character description
  * @param img      the URL of the character image
  * @param affiliation  The JSON Object key in the body of the HTTP response that contains a {@link AffiliationDto}
  * @param combatStyles The JSON Array key in the body of the HTTP response that contains a List of {@link CombatStyleDto}
  */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson will ignore any additional fields present in the JSON
public record CharacterDto(
        Long id,
        String name,
        String gender,
        String race,
        String description,
        String img,
        AffiliationDto affiliation,

        @JsonProperty("combat_style")
        Set<CombatStyleDto> combatStyles
) {
    @Override
    @NotNull
    public String toString() {
        String combatStylesStr = combatStyles == null
                ? "[]"
                : combatStyles.stream()
                    .map(CombatStyleDto::name)
                    .toList()
                    .toString();
        return """
               CharacterDto{
                    id=%d,
                    name="%s",
                    gender="%s",
                    race="%s",
                    description="%s",
                    img="%s",
                    affiliation="%s",
                    combatStyles=%s
                }
               """.formatted(id, name, gender, race, description, img, affiliation.name(), combatStylesStr);
    }
}
