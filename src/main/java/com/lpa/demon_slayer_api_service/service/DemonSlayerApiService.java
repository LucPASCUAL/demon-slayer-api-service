package com.lpa.demon_slayer_api_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpa.demon_slayer_api_service.exception.DemonSlayerApiException;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterResponseDto;
import com.lpa.demon_slayer_api_service.model.dto.CombatStyleDto;
import com.lpa.demon_slayer_api_service.model.dto.character.CharacterSummaryDto;
import com.lpa.demon_slayer_api_service.model.dto.pagination.PageResponseCharacterSummaryDto;
import com.lpa.demon_slayer_api_service.model.dto.pagination.PageResponseCombatStyleDto;
import com.lpa.demon_slayer_api_service.utils.DemonSlayerApiUtils;
import com.lpa.demon_slayer_api_service.model.dto.pagination.PaginationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.*;

/**
 * The DemonSlayerApiService class contains methods for querying the public Demon Slayer API.
 * It uses Flux and Mono types from Project Reactor Java library through Spring WebFlux, Both types are reactive and allow you to manage asynchronous and non-blocking data flows:
 * <ul>
 *     <li>Mono<T> -> 0 to 1 item</li>
 *     <li>Flux<T> -> 0 to N items</li>
 * </ul>
 * @see <a href="https://www.demonslayer-api.com/documentation" target="_blank"> Demon Slayer API Documentation</a>
 * @author Luc Pascual
 * @version 1.0
 */
@Service
public class DemonSlayerApiService {

    private final WebClient webClient;
    private final String characterEndpoint;
    private final String combatStyleEndpoint;

    public DemonSlayerApiService(WebClient.Builder webClientBuilder,
                                 @Value("${api.demonslayer.base.url}") String baseUrl,
                                 @Value("${api.demonslayer.character.endpoint}") String characterEndpoint,
                                 @Value("${api.demonslayer.combat.style.endpoint}") String combatStyleEndpoint) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.characterEndpoint = characterEndpoint;
        this.combatStyleEndpoint = combatStyleEndpoint;
    }

    /**
      * Retrieve the list of Demon Slayer characters from the Demon Slayer public API
      * <strong>Alternative approach without ParallelFlux:</strong>
      * <pre>{@code
      * public List<CharacterSummaryDto> getAllCharacters() {
      *     return fetchPage(1)
      *         .expand(pageResponseDto -> pageResponseDto.pagination().hasNext() ?
      *                 fetchPage(pageResponseDto.pagination().getNextPageNumber())
      *                     .delayElement(Duration.ofMillis(500))
      *                 : Mono.empty()) //expand is used to generate a series of elements from an initial element, recursively, until there is nothing left to generate.
      *         .flatMap(page -> Flux.fromIterable(page.content())) // flatten each page's content
      *         .collectList() // aggregate all elements into a single list
      *         .block(); // block to get the result synchronously
      * }
      * }</pre>
      *
      * @return the list of characters sorted by ID
      */
    public List<CharacterSummaryDto> getAllCharacters() {
        PageResponseCharacterSummaryDto firstPage = fetchPage(characterEndpoint, 1, PageResponseCharacterSummaryDto.class).block();
        if (firstPage == null) return new ArrayList<>();

        int totalPages = firstPage.pagination().totalPages(); //get the total number of pages
        //we create a Flux that outputs the page numbers starting from 2 up to the total number of pages
        List<CharacterSummaryDto> characters = Objects.requireNonNullElse(
                Flux.range(2, totalPages - 1)
                .parallel(5) //converts the Flux into ParallelFlux, a maximum of 5 pages will be retrieved simultaneously, to speed up page retrieval
                .runOn(Schedulers.boundedElastic()) //boundedElastic Scheduler is used for blocking I/O or waiting on a long running calculation operation (network I/O, JDBC database calls...)
                .flatMap(pageNumber -> fetchPage(characterEndpoint, pageNumber, PageResponseCharacterSummaryDto.class) //retrieve each page asynchronously and flatten all these Mono<PageResponseCharacterSummaryDto> into a Flux<PageResponseCharacterSummaryDto>
                        .onErrorResume(page -> Mono.empty())) //ignore if a page failed
                .flatMap(page -> Flux.fromIterable(page.content())) //each page contains a list of characters. flatMap flattens all these lists into a Flux<CharacterSummaryDto>
                .sequential() //converts the ParallelFlux into a Flux
                .startWith(Flux.fromIterable(firstPage.content())) //include characters from the first page
                .collectList()
                .block(), Collections.emptyList() //block to get the result synchronously, return an empty list if characters is null
        );
        return DemonSlayerApiUtils.sortById(List.copyOf(characters)); //immutable list
    }

    /**
      * Retrieve the list of combat styles from the Demon Slayer public API
      *
      * @return the list of combat styles
      */
    public List<CombatStyleDto> getAllCombatStyles() {
        PageResponseCombatStyleDto firstPage = fetchPage(combatStyleEndpoint, 1, PageResponseCombatStyleDto.class).block();
        if (firstPage == null) return new ArrayList<>();

        int totalPages = firstPage.pagination().totalPages();
        List<CombatStyleDto> combatStyles = Objects.requireNonNullElse(
                Flux.range(2, totalPages - 1)
                .parallel(5)
                .runOn(Schedulers.boundedElastic())
                .flatMap(pageNumber -> fetchPage(combatStyleEndpoint, pageNumber, PageResponseCombatStyleDto.class)
                        .onErrorResume(page -> Mono.empty())) //ignore if a page failed
                .flatMap(page -> Flux.fromIterable(page.content()))
                .sequential()
                .startWith(Flux.fromIterable(firstPage.content()))
                .collectList()
                .block(), Collections.emptyList() //if combat styles is null, return an empty list
        );
        return DemonSlayerApiUtils.sortById(List.copyOf(combatStyles)); //immutable list
    }


    /**
      * Retrieve a character using their API ID or name
      *
      * @param id the character ID from API side
      * @return the CharacterDto found
      * @throws DemonSlayerApiException if the server returned 4xx, 5xx HTTP status code or response is null
      */
    public CharacterDto fetchCharacter(Long id, String name) throws DemonSlayerApiException, IllegalArgumentException {
        if (id == null && (name == null || name.isEmpty()))
            throw new DemonSlayerApiException("Provide exactly one of 'id' or 'name'", HttpStatus.BAD_REQUEST);

        CharacterResponseDto response = webClient.get()
                .uri(uriBuilder -> buildCharacterUri(uriBuilder, id, name))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleApiError)
                .bodyToMono(CharacterResponseDto.class).block();

        return Optional.ofNullable(response) //create an Optional<ResponseDto>
                .map(CharacterResponseDto::content) //get the value contained in Optional<ResponseDto> and transforms it into List<CharacterDto> because the result is always in a JSON array
                .filter(list -> !list.isEmpty())
                .map(List::getFirst) //retrieves the first item in the list
                .orElseThrow(() -> new DemonSlayerApiException("Character with id " + id + " not found.", HttpStatus.NOT_FOUND)); //throws a DemonSlayerApiException if response is null

    }

    /**
      * @param endpoint   the Demon Slayer API endpoint
      * @param pageNumber the page number of the characters or combat styles to retrieve from the Demon Slayer public API
      * @param dtoClass   the DTO class
      * @return a {@link Mono} that emits a {@link PageResponseCharacterSummaryDto} or {@link PageResponseCombatStyleDto} containing:
      *                  <ul>
      *                      <li>a list of {@link CharacterDto} or {@link CombatStyleDto} for the requested page</li>
      *                      <li>a {@link PaginationDto} which contains the current page number, and the total number of pages(</li>
      *                  </ul>
      * The Mono completes successfully when the API responds with a valid page, or error with {@link DemonSlayerApiException} if a client or server error occurs.
      */
    private <T> Mono<T> fetchPage(String endpoint, int pageNumber, Class<T> dtoClass) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(endpoint)
                .queryParam("page", pageNumber)
                .queryParam("limit", 10)
                .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleApiError)
                .bodyToMono(dtoClass);
    }

    /**
      * Build the URI depending on the type of search (by ID or by name)
      *
      * @param uriBuilder the URI builder to which we will send a request
      * @param id         the character ID from API side
      * @param name       the character name
      * @return the URI Object to which we will send our HTTP request
      */
    private URI buildCharacterUri(UriBuilder uriBuilder, Long id, String name) {
        uriBuilder.path(characterEndpoint);
        if (id != null) uriBuilder.queryParam("id", id);
        else uriBuilder.queryParam("name", name);
        return uriBuilder.build();
    }

    /**
      * Handle the Demon Slayer public API errors
      *
      * <p>This method intercepts HTTP 4xx and 5xx responses and emits a {@link DemonSlayerApiException}
      * containing the error message returned by the API.</p>
      *
      * <strong>Example of an error returned by the Demon Slayer API</strong>
      * <pre>{@code
      * {
      *     "error": {
      *         "status": 404,
      *         "message": "Im sorry, I couldn't find the character ☹ Please, try again."
      *     }
      *  }
      * }</pre>
      *
      * @param response Spring WebFlux Object representing the raw HTTP response received
      * @return a {@link Mono} that will emit a {@link DemonSlayerApiException} if the server returned 4xx or 5xx HTTP status code
      */
    private Mono<? extends Throwable> handleApiError(ClientResponse response) {
        return response.bodyToMono(String.class).flatMap(body -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode root = mapper.readTree(body);
                String message = root.path("error")
                        .path("message")
                        .asText("Unknown error"); //We retrieve the native error from the Demon Slayer API.
                return Mono.error(
                        new DemonSlayerApiException(message, HttpStatus.valueOf(response.statusCode().value()))
                );

            } catch (Exception _) {
                return Mono.error(
                        new DemonSlayerApiException("Unknown error (invalid JSON response)", HttpStatus.valueOf(response.statusCode().value()))
                );
            }
        });
    }
}
