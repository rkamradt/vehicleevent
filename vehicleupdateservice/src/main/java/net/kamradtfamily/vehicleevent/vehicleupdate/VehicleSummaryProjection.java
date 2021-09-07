package net.kamradtfamily.vehicleevent.vehicleupdate;

import net.kamradtfamily.vehicleevent.api.*;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

@Service
@ProcessingGroup("vehicle-summary")
public class VehicleSummaryProjection {

    private final EntityManager entityManager;
    private final QueryUpdateEmitter queryUpdateEmitter;

    public VehicleSummaryProjection(EntityManager entityManager,
                                 QueryUpdateEmitter queryUpdateEmitter) {
        this.entityManager = entityManager;
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    public void on(VehiclePurchaseEvent event) {
        entityManager.persist(VehicleSummary.builder()
                .id(event.getId())
                .price(event.getPrice())
                .type(event.getType())
                .inductTime(event.getTime())
                .build());

    }

    @EventHandler
    public void on(VehicleSendToLotEvent event) {
        VehicleSummary summary = entityManager.find(VehicleSummary.class, event.getId());
        summary.setLot(event.getLot());
        summary.setToLotTime(event.getTime());

        queryUpdateEmitter.emit(FetchVehicleSummaryQuery.class,
                query -> event.getId().contentEquals(query.getFilter().getId()),
                summary);
    }

    @EventHandler
    public void on(VehicleSellEvent event) {
        VehicleSummary summary = entityManager.find(VehicleSummary.class, event.getId());
        summary.setSellPrice(event.getPrice());
        summary.setSellTime(event.getTime());

        queryUpdateEmitter.emit(FetchVehicleSummaryQuery.class,
                query -> event.getId().contentEquals(query.getFilter().getId()),
                summary);
    }

    @QueryHandler
    public VehicleSummary handle(FetchVehicleSummaryQuery query) {
        TypedQuery<VehicleSummary> jpaQuery = entityManager.createNamedQuery("VehicleSummary.fetch", VehicleSummary.class);
        jpaQuery.setParameter("id", query.getFilter().getId());
        jpaQuery.setFirstResult(query.getOffset());
        jpaQuery.setMaxResults(query.getLimit());
        return jpaQuery.getSingleResult();
    }

}
