package net.kamradtfamily.vehicleevent.vehicleupdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rkamradt.possibly.PossiblyFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

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

    Mono<JsonNode> getLotInfo(String id) {
        return webClient
                .get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .map(PossiblyFunction.of( s -> objectMapper.readTree(s)))
                .doOnNext(p -> p.doOnException(e -> log.error("error getting lot info", e)))
                .flatMap(p -> p.exceptional()
                        ? Mono.error(p.getException().get())
                        : Mono.just(p.getValue().get()))
                .onErrorResume(e -> e instanceof WebClientResponseException, e -> Mono.empty());
    }

    Mono<JsonNode> getLotInfoByName(String name) {
        return webClient
                .get()
                .uri("?name={name}", name)
                .retrieve()
                .bodyToMono(String.class)
                .map(PossiblyFunction.of( s -> objectMapper.readTree(s)))
                .doOnNext(p -> p.doOnException(e -> log.error("error getting lot info", e)))
                .flatMap(p -> p.exceptional()
                        ? Mono.error(p.getException().get())
                        : Mono.just(p.getValue().get()))
                .filter(node -> node.isArray())
                .map(node -> node.get(0))
                .onErrorResume(e -> e instanceof WebClientResponseException, e -> Mono.empty());
    }

}
