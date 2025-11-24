package com.lpa.demon_slayer_api_service.controller;

import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterSummaryDto;
import com.lpa.demon_slayer_api_service.service.DemonSlayerApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemonSlayerApiController {

    private final DemonSlayerApiService demonSlayerApiService;

    @GetMapping("/characters")
    Flux<CharacterSummaryDto> getAllCharacters() {
        return demonSlayerApiService.getAllCharacters();
    }

    @GetMapping("/characters/{id}")
    Mono<CharacterDto> getCharacterById(@PathVariable Long id) {
        return demonSlayerApiService.fetchCharacter(id, null);
    }

    @GetMapping("/characters/search")
    Mono<CharacterDto> getCharacterByParam(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name
    ) {
        return demonSlayerApiService.fetchCharacter(id, name);
    }

    @GetMapping("/combat-styles")
    Flux<CombatStyleDto> getAllCombatStyles() {
        return demonSlayerApiService.getAllCombatStyles();
    }
}
