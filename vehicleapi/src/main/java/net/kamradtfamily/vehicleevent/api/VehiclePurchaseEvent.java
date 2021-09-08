package net.kamradtfamily.vehicleevent.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kamradtfamily.contextlogging.EventContext;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePurchaseEvent {
    String id;
    BigDecimal price;
    String type;
    String time;
    EventContext context;
}
