package net.kamradtfamily.contextlogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;


public interface ContextLogger extends ContextValues {
    Logger LOG = LoggerFactory.getLogger(ContextLogger.class.getName());
    ObjectMapper objectMapper = new ObjectMapper();
    static void logOnNext(Signal s, String message) {
        if(s.isOnNext() && LOG.isInfoEnabled()) {
            ObjectNode jsonNode = createJsonFromSignal(s, message);
            LOG.info(jsonNode.toString());
        }
    }

    static void logWithContext(ContextView context, String message) {
        ObjectNode jsonNode = createJsonFromContext(context, message);
        LOG.info(jsonNode.toString());
    }

    static void logWithContext(EventContext context, String message) {
        ObjectNode jsonNode = createJsonFromContext(context, message);
        LOG.info(jsonNode.toString());
    }

    static void logOnError(Signal s, String message) {
        if(s.isOnError() && LOG.isErrorEnabled()) {
            ObjectNode jsonNode = createJsonFromSignal(s, message);
            LOG.error(jsonNode.toString());
        }
    }

    static ObjectNode createJsonFromSignal(Signal s, String message) {
        ObjectNode jsonNode = objectMapper.createObjectNode()
                .put(MESSAGE, message);
        if (s.hasError()) {
            jsonNode = jsonNode.put(ERROR, s.getThrowable().getMessage())
                    .put(ERROR_TYPE, s.getThrowable().getClass().getSimpleName());
        }
        if (s.hasValue()) {
            jsonNode = jsonNode.put(REQUEST_ID, s.getContextView().getOrDefault(REQUEST_ID, ""))
                    .put(CLIENT_ID, s.getContextView().getOrDefault(CLIENT_ID, ""))
                    .put(ORIGIN, s.getContextView().getOrDefault(ORIGIN, ""));
        }
        return jsonNode;
    }

    static ObjectNode createJsonFromContext(ContextView context, String message) {
        return objectMapper.createObjectNode()
                .put(MESSAGE, message).put(REQUEST_ID, context.getOrDefault(REQUEST_ID, ""))
                .put(CLIENT_ID, context.getOrDefault(CLIENT_ID, ""))
                .put(ORIGIN, context.getOrDefault(ORIGIN, ""));
    }

    static ObjectNode createJsonFromContext(EventContext context, String message) {
        return objectMapper.createObjectNode()
                .put(MESSAGE, message)
                .put(REQUEST_ID, context.getRequestId())
                .put(CLIENT_ID, context.getClientId())
                .put(ORIGIN, context.getOrigin());
    }
}
