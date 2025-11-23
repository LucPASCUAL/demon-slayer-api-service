package com.lpa.demon_slayer_api_service.controller;

import com.lpa.demon_slayer_api_service.exception.DemonSlayerApiException;
import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterSummaryDto;
import com.lpa.demon_slayer_api_service.service.DemonSlayerApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemonSlayerApiController {

    private final DemonSlayerApiService demonSlayerApiService;

    @GetMapping("/characters")
    List<CharacterSummaryDto> getAllCharacters() {
        return demonSlayerApiService.getAllCharacters();
    }

    @GetMapping("/characters/{id}")
    CharacterDto getCharacterById(@PathVariable Long id) throws DemonSlayerApiException {
        return demonSlayerApiService.fetchCharacter(id, null);
    }

    @GetMapping("/characters/search")
    CharacterDto getCharacterByParam(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name
    ) throws DemonSlayerApiException {
        return demonSlayerApiService.fetchCharacter(id, name);
    }

    @GetMapping("/combat-styles")
    List<CombatStyleDto> getAllCombatStyles() {
        return demonSlayerApiService.getAllCombatStyles();
    }
}
