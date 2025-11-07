package net.kamradtfamily.vehicleevent.vehicleupdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.kamradtfamily.vehicleevent.api.VehiclePurchaseCommand;
import net.kamradtfamily.vehicleevent.api.VehicleSellCommand;
import net.kamradtfamily.vehicleevent.api.VehicleSendToLotCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private CommandGateway commandGateway;

    @Mock
    private LotService lotService;

    private ObjectMapper objectMapper;
    private VehicleController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new VehicleController(commandGateway, objectMapper, lotService);
    }

    @Test
    void testRegisterVehicle_success() {
        // Arrange
        VehiclePurchasePayload payload = new VehiclePurchasePayload();
        payload.setPrice("25000.00");
        payload.setType("sedan");

        when(commandGateway.send(any(VehiclePurchaseCommand.class)))
                .thenReturn(CompletableFuture.completedFuture("vehicle-123"));

        // Act
        Mono<String> result = controller.registerVehicle(payload);

        // Assert
        StepVerifier.create(result)
                .expectNext("vehicle-123")
                .verifyComplete();

        ArgumentCaptor<VehiclePurchaseCommand> commandCaptor = ArgumentCaptor.forClass(VehiclePurchaseCommand.class);
        verify(commandGateway).send(commandCaptor.capture());
        VehiclePurchaseCommand command = commandCaptor.getValue();

        assertNotNull(command.getId());
        assertEquals(new BigDecimal("25000.00"), command.getPrice());
        assertEquals("sedan", command.getType());
    }

    @Test
    void testRegisterVehicle_commandGatewayFailure() {
        // Arrange
        VehiclePurchasePayload payload = new VehiclePurchasePayload();
        payload.setPrice("25000.00");
        payload.setType("sedan");

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Command failed"));

        when(commandGateway.send(any(VehiclePurchaseCommand.class)))
                .thenReturn((CompletableFuture) failedFuture);

        // Act
        Mono<String> result = controller.registerVehicle(payload);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testMoveToLot_success() {
        // Arrange
        String vehicleId = "vehicle-123";
        String lotName = "Main Lot";
        String lotId = "lot-456";

        ObjectNode lotNode = objectMapper.createObjectNode();
        lotNode.put("id", lotId);
        lotNode.put("name", lotName);

        when(lotService.getLotInfoByName(lotName))
                .thenReturn(Mono.just(lotNode));
        when(commandGateway.send(any(VehicleSendToLotCommand.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture("success"));

        // Act
        Mono<String> result = controller.moveToLot(vehicleId, lotName);

        // Assert
        StepVerifier.create(result)
                .expectNext("success")
                .verifyComplete();

        ArgumentCaptor<VehicleSendToLotCommand> commandCaptor = ArgumentCaptor.forClass(VehicleSendToLotCommand.class);
        verify(commandGateway).send(commandCaptor.capture());
        VehicleSendToLotCommand command = commandCaptor.getValue();

        assertEquals(vehicleId, command.getId());
        assertEquals(lotId, command.getLot());
    }

    @Test
    void testMoveToLot_lotNotFound() {
        // Arrange
        String vehicleId = "vehicle-123";
        String lotName = "NonExistent Lot";

        when(lotService.getLotInfoByName(lotName))
                .thenReturn(Mono.empty());

        // Act
        Mono<String> result = controller.moveToLot(vehicleId, lotName);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();

        verify(commandGateway, never()).send(any());
    }

    @Test
    void testMoveToLot_lotServiceFailure() {
        // Arrange
        String vehicleId = "vehicle-123";
        String lotName = "Main Lot";

        when(lotService.getLotInfoByName(lotName))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Act
        Mono<String> result = controller.moveToLot(vehicleId, lotName);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(commandGateway, never()).send(any());
    }

    @Test
    void testRegisterSale_success() {
        // Arrange
        String vehicleId = "vehicle-123";
        VehiclePurchasePayload payload = new VehiclePurchasePayload();
        payload.setPrice("28000.00");

        when(commandGateway.send(any(VehicleSellCommand.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture("sale-registered"));

        // Act
        Mono<String> result = controller.registerSale(vehicleId, payload);

        // Assert
        StepVerifier.create(result)
                .expectNext("sale-registered")
                .verifyComplete();

        ArgumentCaptor<VehicleSellCommand> commandCaptor = ArgumentCaptor.forClass(VehicleSellCommand.class);
        verify(commandGateway).send(commandCaptor.capture());
        VehicleSellCommand command = commandCaptor.getValue();

        assertEquals(vehicleId, command.getId());
        assertEquals(new BigDecimal("28000.00"), command.getPrice());
    }

    @Test
    void testRegisterSale_commandFailure() {
        // Arrange
        String vehicleId = "vehicle-123";
        VehiclePurchasePayload payload = new VehiclePurchasePayload();
        payload.setPrice("28000.00");

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalArgumentException("vehicle already sold"));

        when(commandGateway.send(any(VehicleSellCommand.class)))
                .thenReturn((CompletableFuture) failedFuture);

        // Act
        Mono<String> result = controller.registerSale(vehicleId, payload);

        // Assert
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
