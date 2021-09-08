package net.kamradtfamily.vehicleevent.lotupdate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.contextlogging.ContextLogger;
import net.kamradtfamily.contextlogging.EventContext;
import net.kamradtfamily.contextlogging.ServerRequestContextBuilder;
import net.kamradtfamily.vehicleevent.lot.api.LotCreateCommand;
import net.kamradtfamily.vehicleevent.lot.api.LotUpdateCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

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
    Mono<String> registerLot(@RequestBody LotPayload payload,
                             ServerHttpRequest request)
    {
        String id = UUID.randomUUID().toString();
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "adding log " + id);
        return Mono.fromFuture(commandGateway.send(new LotCreateCommand(id,
                payload.getName(), payload.getManager(), EventContext.buildFromContext(context))))
                .cast(String.class)
                .doOnEach(s -> ContextLogger.logOnNext(s, "added lot"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error adding lot"))
                .contextWrite(ctx -> ctx.putAll(context));
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
                           @RequestBody LotPayload payload,
                           ServerHttpRequest request)
    {
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "updating lot " + id);
        return Mono.fromFuture(commandGateway.send(new LotUpdateCommand(id,
                payload.getName(),
                payload.getManager(), EventContext.buildFromContext(context))))
                .cast(String.class)
                .switchIfEmpty(Mono.just("")) // sends that don't create return empty, just replace with a string so logging happens
                .doOnEach(s -> ContextLogger.logOnNext(s, "updated lot"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error updating lot"))
                .contextWrite(ctx -> ctx.putAll(context));
    }

}
