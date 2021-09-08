package net.kamradtfamily.vehicleevent.lotquery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.contextlogging.ContextLogger;
import net.kamradtfamily.contextlogging.ServerRequestContextBuilder;
import net.kamradtfamily.vehicleevent.lot.api.*;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

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
    Mono<LotSummary> findLotById(@PathVariable("id") String id,
                                 ServerHttpRequest request)
    {
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "finding lot " + id);
        return Mono.fromFuture(queryGateway.query(FetchLotSummaryQuery.builder()
                        .filter(new LotSummaryFilter(id))
                        .limit(100)
                        .offset(0)
                        .build(),
                LotSummary.class))
                .doOnEach(s -> ContextLogger.logOnNext(s, "found lot"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error finding lot"))
                .contextWrite(ctx -> ctx.putAll(context));
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
    Mono<List<LotSummary>> findLotByName(@RequestParam("name") String name,
                                         ServerHttpRequest request)
    {
        if(name == null) {
            name = "%";
        }
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "finding lot named " + name);
        return Mono.fromFuture(queryGateway.query(FetchLotByNameQuery.builder()
                        .filter(new LotByNameFilter(name))
                        .limit(100)
                        .offset(0)
                        .build(),
                ResponseTypes.multipleInstancesOf(LotSummary.class)))
                .doOnEach(s -> ContextLogger.logOnNext(s, "found lot"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error finding lot"))
                .contextWrite(ctx -> ctx.putAll(context));
    }
}
