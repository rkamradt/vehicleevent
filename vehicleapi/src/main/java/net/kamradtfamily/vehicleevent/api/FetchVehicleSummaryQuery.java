package net.kamradtfamily.vehicleevent.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchVehicleSummaryQuery {
    int limit;
    int offset;
    VehicleSummaryFilter filter;
}
