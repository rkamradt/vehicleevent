package net.kamradtfamily.vehicleevent.lot.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotCreateEvent {
    String id;
    String name;
    String manager;
    String time;
}
