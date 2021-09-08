package net.kamradtfamily.contextlogging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventContext implements ContextValues {
    String requestId;
    String clientId;
    String origin;
    public static EventContext buildFromContext(ContextView contextView) {
        return EventContext.builder()
                .requestId(contextView.getOrDefault(REQUEST_ID, ""))
                .clientId(contextView.getOrDefault(CLIENT_ID, ""))
                .origin(contextView.getOrDefault(ORIGIN, ""))
                .build();
    }
    public Context createContext() {
        return Context.of(
                REQUEST_ID, requestId,
                CLIENT_ID, clientId,
                ORIGIN, origin
        );
    }
}
