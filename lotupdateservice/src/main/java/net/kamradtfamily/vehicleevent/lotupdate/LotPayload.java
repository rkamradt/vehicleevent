package net.kamradtfamily.vehicleevent.lotupdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotPayload {
    String id;
    String name;
    String manager;
}
