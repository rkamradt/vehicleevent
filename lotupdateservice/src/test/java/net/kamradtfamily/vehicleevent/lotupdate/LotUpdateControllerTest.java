package net.kamradtfamily.vehicleevent.lotupdate;

import net.kamradtfamily.vehicleevent.lot.api.LotCreateCommand;
import net.kamradtfamily.vehicleevent.lot.api.LotUpdateCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotUpdateControllerTest {

    @Mock
    private CommandGateway commandGateway;

    private LotUpdateController controller;

    @BeforeEach
    void setUp() {
        controller = new LotUpdateController(commandGateway);
    }

    @Test
    void testRegisterLot_success() {
        // Arrange
        LotPayload payload = new LotPayload();
        payload.setName("Main Lot");
        payload.setManager("John Doe");

        when(commandGateway.send(any(LotCreateCommand.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture("lot-123"));

        // Act
        Mono<String> result = controller.registerLot(payload);

        // Assert
        StepVerifier.create(result)
                .expectNext("lot-123")
                .verifyComplete();

        ArgumentCaptor<LotCreateCommand> commandCaptor = ArgumentCaptor.forClass(LotCreateCommand.class);
        verify(commandGateway).send(commandCaptor.capture());
        LotCreateCommand command = commandCaptor.getValue();

        assertNotNull(command.getId());
        assertEquals("Main Lot", command.getName());
        assertEquals("John Doe", command.getManager());
    }

    @Test
    void testRegisterLot_commandGatewayFailure() {
        // Arrange
        LotPayload payload = new LotPayload();
        payload.setName("Main Lot");
        payload.setManager("John Doe");

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Command failed"));

        when(commandGateway.send(any(LotCreateCommand.class)))
                .thenReturn((CompletableFuture) failedFuture);

        // Act
        Mono<String> result = controller.registerLot(payload);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testMoveToLot_success() {
        // Arrange
        String lotId = "lot-123";
        LotPayload payload = new LotPayload();
        payload.setName("Updated Main Lot");
        payload.setManager("Jane Smith");

        when(commandGateway.send(any(LotUpdateCommand.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture("success"));

        // Act
        Mono<String> result = controller.moveToLot(lotId, payload);

        // Assert
        StepVerifier.create(result)
                .expectNext("success")
                .verifyComplete();

        ArgumentCaptor<LotUpdateCommand> commandCaptor = ArgumentCaptor.forClass(LotUpdateCommand.class);
        verify(commandGateway).send(commandCaptor.capture());
        LotUpdateCommand command = commandCaptor.getValue();

        assertEquals(lotId, command.getId());
        assertEquals("Updated Main Lot", command.getName());
        assertEquals("Jane Smith", command.getManager());
    }

    @Test
    void testMoveToLot_emptyResponse() {
        // Arrange
        String lotId = "lot-123";
        LotPayload payload = new LotPayload();
        payload.setName("Updated Main Lot");
        payload.setManager("Jane Smith");

        when(commandGateway.send(any(LotUpdateCommand.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture(null));

        // Act
        Mono<String> result = controller.moveToLot(lotId, payload);

        // Assert - switchIfEmpty should convert null to empty string
        StepVerifier.create(result)
                .expectNext("")
                .verifyComplete();
    }

    @Test
    void testMoveToLot_commandGatewayFailure() {
        // Arrange
        String lotId = "lot-123";
        LotPayload payload = new LotPayload();
        payload.setName("Updated Main Lot");
        payload.setManager("Jane Smith");

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Update failed"));

        when(commandGateway.send(any(LotUpdateCommand.class)))
                .thenReturn((CompletableFuture) failedFuture);

        // Act
        Mono<String> result = controller.moveToLot(lotId, payload);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testRegisterLot_withEmptyName() {
        // Arrange
        LotPayload payload = new LotPayload();
        payload.setName("");
        payload.setManager("John Doe");

        when(commandGateway.send(any(LotCreateCommand.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture("lot-123"));

        // Act
        Mono<String> result = controller.registerLot(payload);

        // Assert
        StepVerifier.create(result)
                .expectNext("lot-123")
                .verifyComplete();

        ArgumentCaptor<LotCreateCommand> commandCaptor = ArgumentCaptor.forClass(LotCreateCommand.class);
        verify(commandGateway).send(commandCaptor.capture());
        LotCreateCommand command = commandCaptor.getValue();

        assertEquals("", command.getName());
    }
}
