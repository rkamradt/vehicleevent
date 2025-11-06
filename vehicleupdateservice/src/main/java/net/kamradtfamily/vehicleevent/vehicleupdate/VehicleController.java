package net.kamradtfamily.vehicleevent.vehicleupdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.vehicleevent.api.VehiclePurchaseCommand;
import net.kamradtfamily.vehicleevent.api.VehicleSellCommand;
import net.kamradtfamily.vehicleevent.api.VehicleSendToLotCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    Mono<String> registerVehicle(@RequestBody VehiclePurchasePayload payload)
    {
        String id = UUID.randomUUID().toString();
        BigDecimal price = new BigDecimal(payload.getPrice());
        log.info("Adding vehicle {} with price {} and type {}", id, price, payload.getType());
        return Mono.fromFuture(commandGateway.send(new VehiclePurchaseCommand(id, price, payload.getType())))
                .cast(String.class)
                .doOnNext(result -> log.info("Added vehicle: {}", id))
                .doOnError(error -> log.error("Error adding vehicle {}: {}", id, error.getMessage()));
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
                           @PathVariable("lot") String lot)
    {
        log.info("Moving vehicle {} to lot {}", id, lot);
        return lotService.getLotInfoByName(lot)
                .switchIfEmpty(Mono.error(new NotFoundException(lot)))
                .map(json -> json.findValue("id").asText())
                .flatMap(lid -> Mono.fromFuture(commandGateway.send(new VehicleSendToLotCommand(id, lid))))
                .cast(String.class)
                .switchIfEmpty(Mono.just(""))
                .doOnNext(result -> log.info("Moved vehicle {} to lot {}", id, lot))
                .doOnError(error -> log.error("Error moving vehicle {} to lot {}: {}", id, lot, error.getMessage()));
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
                               @RequestBody VehiclePurchasePayload payload)
    {
        BigDecimal price = new BigDecimal(payload.getPrice());
        log.info("Selling vehicle {} for price {}", id, price);
        return Mono.fromFuture(commandGateway.send(new VehicleSellCommand(id, price)))
                .cast(String.class)
                .switchIfEmpty(Mono.just(""))
                .doOnNext(result -> log.info("Sold vehicle: {}", id))
                .doOnError(error -> log.error("Error selling vehicle {}: {}", id, error.getMessage()));
    }

}
