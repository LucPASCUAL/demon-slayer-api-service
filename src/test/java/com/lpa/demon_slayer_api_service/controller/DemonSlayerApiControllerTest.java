package com.lpa.demon_slayer_api_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpa.demon_slayer_api_service.exception.DemonSlayerApiException;
import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterResponseDto;
import com.lpa.demon_slayer_api_service.model.dto.pagination.PageResponseCharacterSummaryDto;
import com.lpa.demon_slayer_api_service.model.dto.pagination.PageResponseCombatStyleDto;
import com.lpa.demon_slayer_api_service.service.DemonSlayerApiService;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterSummaryDto;
import com.lpa.demon_slayer_api_service.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DemonSlayerApiController.class)
class DemonSlayerApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                .thenReturn(charactersDto);
        mockMvc.perform(get("/api/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getCharacterById_returnsCharacterDto() throws Exception {
        String characterJsonMock = TestUtils.loadJson("character-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        CharacterResponseDto characterResponseDto = mapper.readValue(characterJsonMock, CharacterResponseDto.class);
        assertNotNull(characterResponseDto);
        when(demonSlayerApiService.fetchCharacter(1L, null))
                .thenReturn(characterResponseDto.content().getFirst());
        mockMvc.perform(get("/api/characters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCharacterByName_returnsCharacterDto() throws Exception {
        String characterJsonMock = TestUtils.loadJson("character-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        CharacterResponseDto characterResponseDto = mapper.readValue(characterJsonMock, CharacterResponseDto.class);
        assertNotNull(characterResponseDto);
        when(demonSlayerApiService.fetchCharacter(null, "Tanjiro Kamado"))
                .thenReturn(characterResponseDto.content().getFirst());
        mockMvc.perform(get("/api/characters/search?name=Tanjiro Kamado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tanjiro Kamado"));
    }

    @Test
    void getCombatStyles_returnsListOfCombatStyleDtoSortedById() throws Exception {
        String combatStylesJsonMock = TestUtils.loadJson("combat-styles-mock.json");
        ObjectMapper mapper = new ObjectMapper();
        PageResponseCombatStyleDto pageResponseCombatStyleDto = mapper.readValue(combatStylesJsonMock, PageResponseCombatStyleDto.class);
        assertNotNull(pageResponseCombatStyleDto);
        List<CombatStyleDto> combatStylesDto = pageResponseCombatStyleDto.content();
        when(demonSlayerApiService.getAllCombatStyles())
                .thenReturn(combatStylesDto);
        mockMvc.perform(get("/api/combat-styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
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
        mockMvc.perform(get("/api/characters/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(dsException.getMessage()));
    }
}
