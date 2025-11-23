# Demon Slayer API Service

[![Java](https://img.shields.io/badge/Java-25-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.8-green)](https://spring.io/projects/spring-boot)

A **Spring Boot 3.5.8** and **Java 25** REST API that provides information about Demon Slayer characters and combat styles. 
The application uses **Spring WebFlux** for asynchronous, non-blocking requests to the public Demon Slayer API (https://www.demonslayer-api.com/).  
This project includes unit tests for both the Controller and Service layers with **JUnit 4.13.2** and:
* **MockMvc**: used to test REST controllers without starting the full server.
* **Mockito**: is used to mock the `DemonSlayerApiService` dependencies.
* **MockWebServer**: used to simulate HTTP responses from the public Demon Slayer API for service tests.

Run all tests with:
```
mvn test
```
> ⚠️ **Warning:** As of the date this README is written, Maven **does not** support Java 25.
> This prevents packaging the application locally. Use Docker to build and run the project instead.

---

## Table of Contents

- [Features](#features)
- [Architecture & Code](#architecture--code)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Installation & Running](#installation--running)
- [Usage Examples](#usage-examples)
- [License](#license)

---

## Features

- Retrieve the full list of Demon Slayer characters
- Search for characters by `id` or `name`
- Retrieve all combat styles
- Handle API errors with detailed messages
- Reactive, non-blocking application using **Spring WebFlux**
- Automatic pagination for API requests

---

## Architecture & Code

- **Controller**: `DemonSlayerApiController`  
  Handles REST endpoints `/api/characters` and `/api/combat-styles`.

- **Service**: `DemonSlayerApiService`
    - Uses `WebClient` for asynchronous HTTP requests.
    - Reactive types: `Mono` (0-1 item) and `Flux` (0-N items).
    - Key methods:
        - `getAllCharacters()`: fetches all characters with parallel pagination.
        - `getAllCombatStyles()`: fetches all combat styles with parallel pagination.
        - `fetchCharacter(id, name)`: fetches a character by `id` or `name`.

- **Error Handling**:
    - `handleApiError(ClientResponse response)` converts HTTP 4xx/5xx into custom `DemonSlayerApiException`.

---

## Prerequisites

- Java 25
- Maven 3.9.11+
- Docker (for containerized execution)

---

## Configuration

Create a `.env` file in the root of the project with the following variables:

```env
# SERVER
SERVER_PORT=8081

# DEMON SLAYER API
API_DEMONSLAYER_BASE_URL=https://www.demonslayer-api.com/api/v1
API_DEMONSLAYER_CHARACTER_ENDPOINT=/characters
API_DEMONSLAYER_COMBAT_STYLE_ENDPOINT=/combat-styles
```
In "**application.properties**", reference the environment variables:

```env
# Spring Boot server port
server.port=${SERVER_PORT}

# Demon Slayer API configuration
api.demonslayer.base.url=${API_DEMONSLAYER_BASE_URL}
api.demonslayer.character.endpoint=${API_DEMONSLAYER_CHARACTER_ENDPOINT}
api.demonslayer.combat.style.endpoint=${API_DEMONSLAYER_COMBAT_STYLE_ENDPOINT}
```
> ⚠️ Note: Make sure the .env file is located in the root of your project and loaded when running the Docker container.


## Installation & Running

### Local Execution
> ⚠️ **Warning:** As of the date this README is written, Maven does **not** support Java 25.
> This prevents packaging the application locally. Use Docker to build and run the project instead.

```bash
mvn clean package
java -jar target/demon-slayer-api-service-1.0.0.jar
```

### Docker Execution

From the "**demon-slayer-api-service**" folder, run the following command to build the Demon Slayer API Service Docker image:

```bash
docker build -t demon-slayer-api .
```
Run the container with environment variables from a .env file and map port 8081:

```bash
docker run --env-file .env -p 8081:8081 demon-slayer-api
```
- **--env-file .env**: loads configuration for the application (like server port and API endpoints)
- **-p 8081:8081**: maps container port 8081 to host port 8081
- **demon-slayer-api**: the name of the Docker image

## Usage Examples

Once the application is running, you can interact with the API using `curl` or any HTTP client:

### Get all characters

```bash
curl http://localhost:8081/api/characters
```

### Get a character by ID

```bash
curl http://localhost:8081/api/characters/1
```

### Get a character by name

```bash
curl http://localhost:8081/api/characters/search?name=Tanjiro
```

### Get all combat styles

```bash
curl http://localhost:8081/api/combat-styles
```

## License
This project is licensed under the MIT License. You are free to use, modify, distribute, 
and commercialize the code as long as proper attribution is given. 
See the LICENSE file for details.


