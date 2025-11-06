package net.kamradtfamily.vehicleevent.lotquery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.vehicleevent.lot.api.*;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/lot")
public class LotQueryController {
    private final QueryGateway queryGateway;

    public LotQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }
    @Operation(summary = "Look up a lot by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get lot info",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LotSummary.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid lot supplied",
                    content = @Content) })
    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.OK)
    Mono<LotSummary> findLotById(@PathVariable("id") String id)
    {
        log.info("Finding lot {}", id);
        return Mono.fromFuture(queryGateway.query(FetchLotSummaryQuery.builder()
                        .filter(new LotSummaryFilter(id))
                        .limit(100)
                        .offset(0)
                        .build(),
                LotSummary.class))
                .doOnNext(result -> log.info("Found lot: {}", id))
                .doOnError(error -> log.error("Error finding lot {}: {}", id, error.getMessage()));
    }
    @Operation(summary = "Look up a lot by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get lot info",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LotSummary.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid lot supplied",
                    content = @Content) })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    Mono<List<LotSummary>> findLotByName(@RequestParam("name") String name)
    {
        final String searchName = (name == null) ? "%" : name;
        log.info("Finding lot named {}", searchName);
        return Mono.fromFuture(queryGateway.query(FetchLotByNameQuery.builder()
                        .filter(new LotByNameFilter(searchName))
                        .limit(100)
                        .offset(0)
                        .build(),
                ResponseTypes.multipleInstancesOf(LotSummary.class)))
                .doOnNext(result -> log.info("Found lot: {}", searchName))
                .doOnError(error -> log.error("Error finding lot {}: {}", searchName, error.getMessage()));
    }
}
