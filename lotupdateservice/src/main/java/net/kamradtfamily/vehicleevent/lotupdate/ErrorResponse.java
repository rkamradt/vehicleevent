package net.kamradtfamily.vehicleevent.lotupdate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String errorCode;
    private String message;
    private String requestId;
    private Instant timestamp;
    private String path;
}
