package net.kamradtfamily.vehicleevent.vehicleupdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.contextlogging.ContextLogger;
import net.kamradtfamily.contextlogging.EventContext;
import net.kamradtfamily.contextlogging.ServerRequestContextBuilder;
import net.kamradtfamily.vehicleevent.api.VehiclePurchaseCommand;
import net.kamradtfamily.vehicleevent.api.VehicleSellCommand;
import net.kamradtfamily.vehicleevent.api.VehicleSendToLotCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.util.context.ContextView;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/vehicle")
public class VehicleController {
    private final CommandGateway commandGateway;
    private final LotService lotService;
    private final ObjectMapper objectMapper;

    public VehicleController(CommandGateway commandGateway,
                             ObjectMapper objectMapper,
                             LotService lotService) {
        this.commandGateway = commandGateway;
        this.lotService = lotService;
        this.objectMapper = objectMapper;
    }
    @Operation(summary = "Register a new vehicle to the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "vehicle purchase registered",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VehiclePurchasePayload.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle supplied",
                    content = @Content) })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    Mono<String> registerVehicle(@RequestBody VehiclePurchasePayload payload,
                                 ServerHttpRequest request)
    {
        String id = UUID.randomUUID().toString();
        BigDecimal price = new BigDecimal(payload.getPrice());
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "adding vehicle " + id);
        return Mono.fromFuture(commandGateway.send(new VehiclePurchaseCommand(id,
                price, payload.getType(), EventContext.buildFromContext(context))))
                .cast(String.class)
                .doOnEach(s -> ContextLogger.logOnNext(s, "added vehicle"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error adding vehicle"))
                .contextWrite(ctx -> ctx.putAll(context));
    }

    @Operation(summary = "Move a vehicle to a new lot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "vehicle move registered",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VehiclePurchasePayload.class)) }),
            @ApiResponse(responseCode = "404", description = "vehicle or lot not found",
                    content = @Content) })
    @PutMapping(path = "move/{id}/{lot}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Mono<String> moveToLot(@PathVariable("id") String id,
                           @PathVariable("lot") String lot,
                           ServerHttpRequest request)
    {
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "moving vehicle " + id);
        return lotService.getLotInfoByName(lot, context)
                .switchIfEmpty(Mono.error(new NotFoundException(lot)))
                .map(json -> json.findValue("id").asText())
                .flatMap(lid -> Mono.fromFuture(commandGateway.send(new VehicleSendToLotCommand(id,
                        lid, EventContext.buildFromContext(context)))))
                .cast(String.class)
                .switchIfEmpty(Mono.just("")) // sends that don't create return empty, just replace with a string so logging happens
                .doOnEach(s -> ContextLogger.logOnNext(s, "moved vehicle"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error moving vehicle"))
                .contextWrite(ctx -> ctx.putAll(context));
    }

    @Operation(summary = "Register a vehicle sale")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "vehicle sale registered",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VehiclePurchasePayload.class)) }),
            @ApiResponse(responseCode = "404", description = "Vehicle not found",
                    content = @Content) })
    @PutMapping(path = "sell/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Mono<String> registerSale(@PathVariable("id") String id,
                               @RequestBody VehiclePurchasePayload payload,
                              ServerHttpRequest request)
    {
        BigDecimal price = new BigDecimal(payload.getPrice());
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "selling vehicle " + id);
        return Mono.fromFuture(commandGateway.send(new VehicleSellCommand(id,
                price, EventContext.buildFromContext(context))))
                .cast(String.class)
                .switchIfEmpty(Mono.just("")) // sends that don't create return empty, just replace with a string so logging happens
                .doOnEach(s -> ContextLogger.logOnNext(s, "sold vehicle"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error selling vehicle"))
                .contextWrite(ctx -> ctx.putAll(context));
    }

}
