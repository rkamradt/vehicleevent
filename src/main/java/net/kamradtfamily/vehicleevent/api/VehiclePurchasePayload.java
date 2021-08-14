package net.kamradtfamily.vehicleevent.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePurchasePayload {
    String id;
    String price;
    String type;
}
