package net.kamradtfamily.vehicleevent.lot.command;

import net.kamradtfamily.vehicleevent.lot.api.*;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
@ProcessingGroup("lot-summary")
public class LotSummaryProjection {

    private final EntityManager entityManager;
    private final QueryUpdateEmitter queryUpdateEmitter;

    public LotSummaryProjection(EntityManager entityManager,
                                QueryUpdateEmitter queryUpdateEmitter) {
        this.entityManager = entityManager;
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    public void on(LotCreateEvent event) {
        entityManager.persist(LotSummary.builder()
                .id(event.getId())
                .name(event.getName())
                .manager(event.getManager())
                .createTime(event.getTime())
                .build());

    }

    @EventHandler
    public void on(LotUpdateEvent event) {
        LotSummary summary = entityManager.find(LotSummary.class, event.getId());
        summary.setName(event.getName());
        summary.setManager(event.getManager());
        summary.setUpdateTime(event.getTime());

        queryUpdateEmitter.emit(FetchLotSummaryQuery.class,
                query -> event.getId().contentEquals(query.getFilter().getId()),
                summary);
    }

    @QueryHandler
    public LotSummary handle(FetchLotSummaryQuery query) {
        TypedQuery<LotSummary> jpaQuery = entityManager.createNamedQuery("LotSummary.fetch", LotSummary.class);
        jpaQuery.setParameter("id", query.getFilter().getId());
        jpaQuery.setFirstResult(query.getOffset());
        jpaQuery.setMaxResults(query.getLimit());
        return jpaQuery.getSingleResult();
    }

    @QueryHandler
    public List<LotSummary> handleByName(FetchLotByNameQuery query) {
        TypedQuery<LotSummary> jpaQuery = entityManager.createNamedQuery("LotByName.fetch", LotSummary.class);
        jpaQuery.setParameter("name", query.getFilter().getName());
        jpaQuery.setFirstResult(query.getOffset());
        jpaQuery.setMaxResults(query.getLimit());
        return jpaQuery.getResultList();
    }

}
