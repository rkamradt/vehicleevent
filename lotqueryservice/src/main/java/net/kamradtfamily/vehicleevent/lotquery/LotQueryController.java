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
        return Mono.fromFuture(queryGateway.query(FetchLotSummaryQuery.builder()
                        .filter(new LotSummaryFilter(id))
                        .limit(100)
                        .offset(0)
                        .build(),
                LotSummary.class));
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
        if(name == null) {
            name = "";
        }
        return Mono.fromFuture(queryGateway.query(FetchLotByNameQuery.builder()
                        .filter(new LotByNameFilter(name))
                        .limit(100)
                        .offset(0)
                        .build(),
                ResponseTypes.multipleInstancesOf(LotSummary.class)));
    }
}
