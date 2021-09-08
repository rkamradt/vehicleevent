package net.kamradtfamily.vehicleevent.lot.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kamradtfamily.contextlogging.EventContext;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotCreateEvent {
    String id;
    String name;
    String manager;
    String time;
    EventContext context;
}
