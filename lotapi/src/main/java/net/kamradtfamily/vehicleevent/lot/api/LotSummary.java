package net.kamradtfamily.vehicleevent.lot.api;

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
@NamedQueries({
        @NamedQuery(
                name = "LotSummary.fetch",
                query = "SELECT c FROM LotSummary c WHERE c.id = :id"
        ),
        @NamedQuery(
                name = "LotByName.fetch",
                query = "SELECT c FROM LotSummary c WHERE c.name LIKE :name"
        )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotSummary {
    @Id
    String id;
    String name;
    String manager;
    String createTime;
    String updateTime;
}
