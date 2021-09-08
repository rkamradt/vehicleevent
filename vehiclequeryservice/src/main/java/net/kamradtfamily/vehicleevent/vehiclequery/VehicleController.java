package net.kamradtfamily.vehicleevent.vehiclequery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.contextlogging.ContextLogger;
import net.kamradtfamily.contextlogging.ServerRequestContextBuilder;
import net.kamradtfamily.vehicleevent.api.FetchVehicleSummaryQuery;
import net.kamradtfamily.vehicleevent.api.VehicleSummary;
import net.kamradtfamily.vehicleevent.api.VehicleSummaryFilter;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Slf4j
@RestController
@RequestMapping("/vehicle")
public class VehicleController {
    private final QueryGateway queryGateway;

    public VehicleController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }
    @Operation(summary = "Look up a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get vehicle info",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VehicleSummary.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle supplied",
                    content = @Content) })
    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.CREATED)
    Mono<VehicleSummary> findVehicle(@PathVariable("id") String id,
                                     ServerHttpRequest request)
    {
        ContextView context = ServerRequestContextBuilder.build(request);
        ContextLogger.logWithContext(context, "finding vehicle " + id);
        return Mono.fromFuture(queryGateway.query(FetchVehicleSummaryQuery.builder()
                .filter(new VehicleSummaryFilter(id))
                .limit(100)
                .offset(0)
                .build(),
                VehicleSummary.class))
                .doOnEach(s -> ContextLogger.logOnNext(s, "found vehicle"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error finding vehicle"))
                .contextWrite(ctx -> ctx.putAll(context));
    }
}
