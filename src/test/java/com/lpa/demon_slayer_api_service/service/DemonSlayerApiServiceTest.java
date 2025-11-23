package com.lpa.demon_slayer_api_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpa.demon_slayer_api_service.exception.DemonSlayerApiException;
import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterSummaryDto;
import com.lpa.demon_slayer_api_service.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
  * This class is used to test the Demon Slayer public API. We use a MockWebServer capable of simulating an HTTP response
  *
  */
@Slf4j
class DemonSlayerApiServiceTest {

    private static MockWebServer mockWebServer;
    private DemonSlayerApiService demonSlayerApiService;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
        log.info("MockWebServer has been shut down");
    }

    @BeforeEach
    void initialize() {
        String mockBaseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        this.demonSlayerApiService = new DemonSlayerApiService(WebClient.builder(), mockBaseUrl,
                "/characters",
                "/combat-styles"
                );
        log.info("MockWebServer running on {}", mockBaseUrl);
    }

    @Test
    void getAllCharacters_returnsListOfCharacterDtoSortedById() throws IOException, InterruptedException {
        String charactersJsonMock = TestUtils.loadJson("characters-mock.json");
        enqueueMockServer(charactersJsonMock, MediaType.APPLICATION_JSON, HttpStatus.OK);
        List<CharacterSummaryDto> charactersDto =  demonSlayerApiService.getAllCharacters();
        checkRequest(HttpMethod.GET, "/characters?page=1&limit=10");
        CharacterSummaryDto firstCharacterDto = charactersDto.getFirst();
        log.info("First character is: \n{}", firstCharacterDto.toString());
        assertEquals(1, firstCharacterDto.id());
    }

    @Test
    void getCharacterById_returnsCharacterDto() throws DemonSlayerApiException, InterruptedException, IOException {
        String characterJsonMock = TestUtils.loadJson("character-mock.json");
        enqueueMockServer(characterJsonMock, MediaType.APPLICATION_JSON, HttpStatus.OK);
        CharacterDto characterDto = demonSlayerApiService.fetchCharacter(1L, null);
        checkRequest(HttpMethod.GET, "/characters?id=1");
        log.info("Character is: \n{}", characterDto.toString());
        assertEquals(1, characterDto.id());
    }

    @Test
    void getCharacterByName_returnsCharacterDto() throws DemonSlayerApiException, InterruptedException, IOException {
        String characterJsonMock = TestUtils.loadJson("character-mock.json");
        String name = "Tanjiro Kamado";
        enqueueMockServer(characterJsonMock, MediaType.APPLICATION_JSON, HttpStatus.OK);
        CharacterDto characterDto = demonSlayerApiService.fetchCharacter(null, name);
        checkRequest(HttpMethod.GET, "/characters?name=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8));
        log.info("Character is: \n{}", characterDto.toString());
        assertEquals("Tanjiro Kamado", characterDto.name());
    }

    @Test
    void getAllCombatStyles_returnsListOfCombatStyleDtoSortedById() throws IOException, InterruptedException {
        String combatStylesJsonMock = TestUtils.loadJson("combat-styles-mock.json");
        enqueueMockServer(combatStylesJsonMock, MediaType.APPLICATION_JSON, HttpStatus.OK);
        List<CombatStyleDto> combatStylesDto =  demonSlayerApiService.getAllCombatStyles();
        checkRequest(HttpMethod.GET, "/combat-styles?page=1&limit=10");
        CombatStyleDto firstCombatStyle = combatStylesDto.getFirst();
        log.info("First combat style is: \n{}", firstCombatStyle.toString());
        assertEquals(1, firstCombatStyle.id());
    }

    @Test
    void getCharacterById_404_shouldReturnDemonSlayerException() throws DemonSlayerApiException, InterruptedException, IOException {
        String errorJsonMock = TestUtils.loadJson("character-not-found-mock.json");
        enqueueMockServer(errorJsonMock, MediaType.APPLICATION_JSON, HttpStatus.NOT_FOUND);
        Throwable thrown = assertThrows(Throwable.class,
                () -> demonSlayerApiService.fetchCharacter(999L, null)); //because DemonSlayerApiException is encapsulated in a reactive stream, the test captures everything as a Throwable
        checkRequest(HttpMethod.GET, "/characters?id=" + 999L);
        assertInstanceOf(DemonSlayerApiException.class, thrown.getCause()); //check the actual cause
        DemonSlayerApiException dsEx = (DemonSlayerApiException) thrown.getCause();
        log.info("The error message is \"{}\"", dsEx.getMessage());
        log.info("The HTTP status code is {}", dsEx.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, dsEx.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(errorJsonMock);
        JsonNode errorNode = rootNode.path("error");
        String errorMessage = errorNode.path("message").asText();
        assertEquals(errorMessage, dsEx.getMessage());

    }

    /**
      * Method used to verify the executed request
      * @param httpMethod  the HTTP method used (GET, POST...)
      * @param url  the URL called
      * @throws InterruptedException if the thread is interrupted
      */
    private void checkRequest(HttpMethod httpMethod, String url) throws InterruptedException {
        RecordedRequest request = mockWebServer.takeRequest();
        assertNotNull(request.getMethod());
        assertEquals(httpMethod.name(), request.getMethod());
        assertEquals(url, request.getPath()); //we check the URL called
    }

    /**
      * Method used to queue a response from the MockWebServer
      *
      * @param body the body of the returned response
      * @param contentType the content-type of the returned response
      * @param statusCode the HTTP status code of the returned response
      */
    private void enqueueMockServer(String body, MediaType contentType, HttpStatus statusCode) {
        log.info("Enqueueing mock response with status {} and contentType {}", statusCode.value(), contentType);
        log.info("Mock response: {}", body);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(statusCode.value())
                .setBody(body)
                .addHeader("Content-Type", contentType)
        );
    }
}
