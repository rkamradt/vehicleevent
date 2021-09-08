package net.kamradtfamily.vehicleevent.vehicleupdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rkamradt.possibly.PossiblyFunction;
import lombok.extern.slf4j.Slf4j;
import net.kamradtfamily.contextlogging.ContextLogger;
import net.kamradtfamily.contextlogging.ServerRequestContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Slf4j
@Component
public class LotService {
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    public LotService(
            @Value("${lot.query.service.url}") String lotQueryServiceUrl,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(lotQueryServiceUrl)
                .build();
    }

    Mono<JsonNode> getLotInfo(String id, ContextView contextView) {
        return webClient
                .get()
                .uri("/{id}", id)
                .headers(httpHeaders -> ServerRequestContextBuilder.addHeaders(contextView, httpHeaders))
                .retrieve()
                .bodyToMono(String.class)
                .map(PossiblyFunction.of( s -> objectMapper.readTree(s)))
                .flatMap(p -> p.exceptional()
                        ? Mono.error(p.getException().get())
                        : Mono.just(p.getValue().get()))
                .doOnEach(s -> ContextLogger.logOnNext(s, "found lot"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error finding lot"))
                .onErrorResume(e -> e instanceof WebClientResponseException, e -> Mono.empty())
                .contextWrite(context -> context.putAll(contextView));
    }

    Mono<JsonNode> getLotInfoByName(String name, ContextView contextView) {
        return webClient
                .get()
                .uri("?name={name}", name)
                .headers(httpHeaders -> ServerRequestContextBuilder.addHeaders(contextView, httpHeaders))
                .retrieve()
                .bodyToMono(String.class)
                .map(PossiblyFunction.of( s -> objectMapper.readTree(s)))
                .flatMap(p -> p.exceptional()
                        ? Mono.error(p.getException().get())
                        : Mono.just(p.getValue().get()))
                .doOnEach(s -> ContextLogger.logOnNext(s, "found lot"))
                .doOnEach(s -> ContextLogger.logOnError(s, "error finding lot"))
                .filter(node -> node.isArray())
                .map(node -> node.get(0))
                .onErrorResume(e -> e instanceof WebClientResponseException, e -> Mono.empty())
                .contextWrite(context -> context.putAll(contextView));
    }

}
