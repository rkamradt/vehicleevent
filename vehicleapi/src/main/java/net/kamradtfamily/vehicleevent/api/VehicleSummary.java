package net.kamradtfamily.vehicleevent.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.math.BigDecimal;

@Entity
@NamedQueries(
    @NamedQuery(
        name = "VehicleSummary.fetch",
        query = "SELECT c FROM VehicleSummary c WHERE c.id = :id"
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSummary {
    @Id
    String id;
    BigDecimal price;
    String type;
    String lot;
    BigDecimal sellPrice;
    String inductTime;
    String toLotTime;
    String sellTime;
}
