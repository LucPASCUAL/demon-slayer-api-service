package com.lpa.demon_slayer_api_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpa.demon_slayer_api_service.exception.DemonSlayerApiException;
import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterResponseDto;
import com.lpa.demon_slayer_api_service.model.dto.pagination.PageResponseCharacterSummaryDto;
import com.lpa.demon_slayer_api_service.model.dto.pagination.PageResponseCombatStyleDto;
import com.lpa.demon_slayer_api_service.service.DemonSlayerApiService;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterSummaryDto;
import com.lpa.demon_slayer_api_service.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebFluxTest(DemonSlayerApiController.class)
class DemonSlayerApiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DemonSlayerApiService demonSlayerApiService;

    @Test
    void getAllCharacters_returnsListOfCharacterDtoSortedById() throws Exception {
        String charactersJsonMock = TestUtils.loadJson("characters-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        PageResponseCharacterSummaryDto pageResponseCharacterSummary = mapper.readValue(charactersJsonMock, PageResponseCharacterSummaryDto.class);
        assertNotNull(pageResponseCharacterSummary);
        List<CharacterSummaryDto> charactersDto = pageResponseCharacterSummary.content();
        when(demonSlayerApiService.getAllCharacters())
                .thenReturn(Flux.fromIterable(charactersDto));
        webTestClient.get()
                .uri("/api/characters")
                .exchange() //sends the simulated HTTP request to our controller
                .expectStatus().isOk()
                .expectBodyList(CharacterSummaryDto.class)
                .hasSize(charactersDto.size())
                .consumeWith(response -> { //allows you to obtain the body of the response and make assertions on it
                    assertNotNull(response.getResponseBody());
                    assertEquals(1, response.getResponseBody().getFirst().id());
                });
    }

    @Test
    void getCharacterById_returnsCharacterDto() throws Exception {
        String characterJsonMock = TestUtils.loadJson("character-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        CharacterResponseDto characterResponseDto = mapper.readValue(characterJsonMock, CharacterResponseDto.class);
        CharacterDto characterDto = characterResponseDto.content().getFirst(); //we obtain the character contained in th response
        when(demonSlayerApiService.fetchCharacter(1L, null))
                .thenReturn(Mono.just(characterDto));
        webTestClient.get()
                .uri("/api/characters/1")
                .exchange() //sends the simulated HTTP request to our controller
                .expectStatus().isOk()
                .expectBody(CharacterDto.class)
                .consumeWith(response -> { //allows you to obtain the body of the response and make assertions on it
                    assertNotNull(response.getResponseBody());
                    assertEquals(response.getResponseBody().id(), characterDto.id());
                });
    }

    @Test
    void getCharacterByName_returnsCharacterDto() throws Exception {
        String characterJsonMock = TestUtils.loadJson("character-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        CharacterResponseDto characterResponseDto = mapper.readValue(characterJsonMock, CharacterResponseDto.class);
        CharacterDto characterDto = characterResponseDto.content().getFirst(); //we obtain the character contained in th response
        when(demonSlayerApiService.fetchCharacter(null, "Tanjiro Kamado"))
                .thenReturn(Mono.just(characterDto));
        webTestClient.get()
                .uri("/api/characters/search?name=Tanjiro Kamado")
                .exchange() //sends the simulated HTTP request to our controller
                .expectStatus().isOk()
                .expectBody(CharacterDto.class)
                .consumeWith(response -> { //allows you to obtain the body of the response and make assertions on it
                    assertNotNull(response.getResponseBody());
                    assertEquals(response.getResponseBody().name(), characterDto.name());
                });
    }

    @Test
    void getCombatStyles_returnsListOfCombatStyleDtoSortedById() throws Exception {
        String combatStylesJsonMock = TestUtils.loadJson("combat-styles-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        PageResponseCombatStyleDto pageResponseCombatStyleDto = mapper.readValue(combatStylesJsonMock, PageResponseCombatStyleDto.class);
        assertNotNull(pageResponseCombatStyleDto);
        List<CombatStyleDto> combatStylesDto = pageResponseCombatStyleDto.content();
        when(demonSlayerApiService.getAllCombatStyles())
                .thenReturn(Flux.fromIterable(combatStylesDto));
        webTestClient.get()
                .uri("/api/combat-styles")
                .exchange() //sends the simulated HTTP request to our controller
                .expectStatus().isOk()
                .expectBodyList(CombatStyleDto.class)
                .hasSize(combatStylesDto.size())
                .consumeWith(response -> { //allows you to obtain the body of the response and make assertions on it
                    assertNotNull(response.getResponseBody());
                    assertEquals(1, response.getResponseBody().getFirst().id());
                });
    }

    @Test
    void getCharacterById_404_shouldReturnNotFound() throws Exception {
        String errorJsonMock = TestUtils.loadJson("character-not-found-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(errorJsonMock);
        JsonNode errorNode = rootNode.path("error");
        String errorMessage = errorNode.path("message").asText(); //we retrieve the error message in the JSON mock.
        DemonSlayerApiException dsException = new DemonSlayerApiException(
                errorMessage,
                HttpStatus.NOT_FOUND
        );
        when(demonSlayerApiService.fetchCharacter(999L, null))
                .thenThrow(dsException);
        webTestClient.get()
                .uri("/api/characters/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo(dsException.getMessage());

    }
}
