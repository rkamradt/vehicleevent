package net.kamradtfamily.contextlogging;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.UUID;

public interface ServerRequestContextBuilder extends ContextValues{
    String REQUEST_ID_HEADER = "x-request-id";
    String CLIENT_ID_HEADER = "x-client-id";
    String ORIGIN_HEADER = "x-origin";
    String AD_HOC_REQUEST_ID_PREFIX = "AHRID-";
    String NO_CLIENT = "no client identified";
    String NO_ORIGIN = "no origin identified";
    static Context build(ServerHttpRequest request) {
        return Context.of(REQUEST_ID,
                request.getHeaders()
                        .getOrDefault(REQUEST_ID_HEADER,
                                List.of(AD_HOC_REQUEST_ID_PREFIX +
                                UUID.randomUUID())).get(0),
                CLIENT_ID,
                request.getHeaders()
                        .getOrDefault(CLIENT_ID_HEADER,
                                List.of(NO_CLIENT))
                        .get(0),
                ORIGIN,
                request.getHeaders()
                        .getOrDefault(ORIGIN_HEADER,
                                List.of(NO_ORIGIN))
                        .get(0));
    }

    static void addHeaders(ContextView contextView, HttpHeaders httpHeaders) {
        httpHeaders.set(REQUEST_ID_HEADER, contextView.getOrDefault(REQUEST_ID, ""));
        httpHeaders.set(CLIENT_ID_HEADER, contextView.getOrDefault(CLIENT_ID, ""));
        httpHeaders.set(ORIGIN_HEADER, "Vehicle Update Service");
    }
}
