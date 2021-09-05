package net.kamradtfamily.vehicleevent.lot.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchLotByNameQuery {
    int limit;
    int offset;
    LotByNameFilter filter;
}
