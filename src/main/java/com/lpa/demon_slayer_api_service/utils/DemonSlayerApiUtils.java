package com.lpa.demon_slayer_api_service.utils;


import com.lpa.demon_slayer_api_service.model.Identifiable;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterSummaryDto;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class DemonSlayerApiUtils {

    private DemonSlayerApiUtils() {
        throw new IllegalStateException("Utility class");
    }

    /** Sort a list of elements by ID
      *
      * @param list       an unsorted list
      * @return     a list of elements sorted by ID
      */
    public static <T extends Identifiable> List<T> sortById(List<T> list) {
        if (list == null || list.isEmpty())
            return list;

        return list.stream()
                .sorted(Comparator.comparing(Identifiable::id))
                .collect(Collectors.toList());
    }

    /** Sort a list of {@link CharacterSummaryDto} by name
      *
      * @param characters       an unsorted list of characters
      * @return     a list of characters sorted by name
      */
    public static List<CharacterSummaryDto> sortCharactersByNameAlphabetically(List<CharacterSummaryDto> characters) {
        if (characters == null || characters.isEmpty())
            return characters;

        return characters.stream()
                .sorted(Comparator.comparing(CharacterSummaryDto::name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
