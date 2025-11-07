package net.kamradtfamily.vehicleevent.vehicleupdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class LotServiceTest {

    private MockWebServer mockWebServer;
    private LotService lotService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        String baseUrl = mockWebServer.url("/").toString();

        lotService = new LotService(
                baseUrl,
                WebClient.builder(),
                objectMapper
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetLotInfo_success() {
        // Arrange
        String lotId = "lot-123";
        String lotJson = "{\"id\":\"lot-123\",\"name\":\"Main Lot\",\"capacity\":100}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(lotJson)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        StepVerifier.create(lotService.getLotInfo(lotId))
                .expectNextMatches(node -> {
                    return node.get("id").asText().equals(lotId) &&
                            node.get("name").asText().equals("Main Lot") &&
                            node.get("capacity").asInt() == 100;
                })
                .verifyComplete();
    }

    @Test
    void testGetLotInfo_notFound() {
        // Arrange
        String lotId = "nonexistent-lot";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not found"));

        // Act & Assert
        StepVerifier.create(lotService.getLotInfo(lotId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testGetLotInfo_serverError() {
        // Arrange
        String lotId = "lot-123";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error"));

        // Act & Assert
        StepVerifier.create(lotService.getLotInfo(lotId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testGetLotInfo_invalidJson() {
        // Arrange
        String lotId = "lot-123";

        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json")
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        StepVerifier.create(lotService.getLotInfo(lotId))
                .expectError()
                .verify();
    }

    @Test
    void testGetLotInfoByName_success() {
        // Arrange
        String lotName = "Main Lot";
        String lotJson = "[{\"id\":\"lot-123\",\"name\":\"Main Lot\",\"capacity\":100}]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(lotJson)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        StepVerifier.create(lotService.getLotInfoByName(lotName))
                .expectNextMatches(node -> {
                    return node.get("id").asText().equals("lot-123") &&
                            node.get("name").asText().equals(lotName) &&
                            node.get("capacity").asInt() == 100;
                })
                .verifyComplete();
    }

    @Test
    void testGetLotInfoByName_emptyArray() {
        // Arrange
        String lotName = "Nonexistent Lot";
        String lotJson = "[]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(lotJson)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert - empty array causes NullPointerException when accessing element 0
        StepVerifier.create(lotService.getLotInfoByName(lotName))
                .expectError(NullPointerException.class)
                .verify();
    }

    @Test
    void testGetLotInfoByName_multipleResults() {
        // Arrange
        String lotName = "Main Lot";
        String lotJson = "[{\"id\":\"lot-123\",\"name\":\"Main Lot\",\"capacity\":100}," +
                "{\"id\":\"lot-456\",\"name\":\"Main Lot 2\",\"capacity\":150}]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(lotJson)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert - should return first element
        StepVerifier.create(lotService.getLotInfoByName(lotName))
                .expectNextMatches(node -> {
                    return node.get("id").asText().equals("lot-123");
                })
                .verifyComplete();
    }

    @Test
    void testGetLotInfoByName_notFound() {
        // Arrange
        String lotName = "Nonexistent Lot";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not found"));

        // Act & Assert
        StepVerifier.create(lotService.getLotInfoByName(lotName))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testGetLotInfoByName_nonArrayResponse() {
        // Arrange
        String lotName = "Main Lot";
        String lotJson = "{\"id\":\"lot-123\",\"name\":\"Main Lot\"}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(lotJson)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert - filter should exclude non-array results
        StepVerifier.create(lotService.getLotInfoByName(lotName))
                .expectNextCount(0)
                .verifyComplete();
    }
}
