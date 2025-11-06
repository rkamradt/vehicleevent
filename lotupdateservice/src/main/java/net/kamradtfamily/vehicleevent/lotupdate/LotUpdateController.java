package net.kamradtfamily.vehicleevent.lotupdate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.vehicleevent.lot.api.LotCreateCommand;
import net.kamradtfamily.vehicleevent.lot.api.LotUpdateCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/lot")
public class LotUpdateController {
    private final CommandGateway commandGateway;

    public LotUpdateController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    @Operation(summary = "Register a new lot to the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "lot registered",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LotPayload.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid lot supplied",
                    content = @Content) })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    Mono<String> registerLot(@RequestBody LotPayload payload)
    {
        String id = UUID.randomUUID().toString();
        log.info("Adding lot {}", id);
        return Mono.fromFuture(commandGateway.send(new LotCreateCommand(id,
                payload.getName(), payload.getManager())))
                .cast(String.class)
                .doOnNext(result -> log.info("Added lot: {}", id))
                .doOnError(error -> log.error("Error adding lot {}: {}", id, error.getMessage()));
    }


    @Operation(summary = "Update a lot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "lot updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LotPayload.class)) }),
            @ApiResponse(responseCode = "404", description = "lot not found",
                    content = @Content) })
    @PutMapping(path = "move/{id}/{lot}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Mono<String> moveToLot(@PathVariable("id") String id,
                           @RequestBody LotPayload payload)
    {
        log.info("Updating lot {}", id);
        return Mono.fromFuture(commandGateway.send(new LotUpdateCommand(id,
                payload.getName(),
                payload.getManager())))
                .cast(String.class)
                .switchIfEmpty(Mono.just("")) // sends that don't create return empty, just replace with a string so logging happens
                .doOnNext(result -> log.info("Updated lot: {}", id))
                .doOnError(error -> log.error("Error updating lot {}: {}", id, error.getMessage()));
    }

}
