/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.collection.error.CollectionException;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.domain.v1.ChildEntity;
import com.cloudimpl.outstack.runtime.domain.v1.DomainEventException;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.EntityHelper;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author nuwan
 * @param <T>
 */
public abstract class EventRepositoy<T extends RootEntity> {

    private final String rsPrefix;
    private final Class<T> rootType;
    private final EventStream eventStream;
    private final EntityCache mapStableCache;

    public EventRepositoy(String rsPrefix, Class<T> rootType, EventStream eventStream) {
        this.rsPrefix = rsPrefix;
        this.rootType = rootType;
        this.mapStableCache = new EntityCache(1000, Duration.ofHours(1));
        this.eventStream = eventStream;
        this.eventStream.flux().publishOn(Schedulers.parallel()).doOnNext(this::onEvent).subscribe();
    }

    public void publish(EventTxList txList) {
        txList.getEvents().forEach(this::publish);
    }

    public  T createRootEntity(String tenantId, String entityId) {
        checkTenantEligibility(tenantId);
        T t = (T) loadRootEntityByBrn(RootEntity.makeRN(rootType, entityId, tenantId));
        if (t != null) {
            throw new DomainEventException("root entity {0} already exist", t.getRN());
        }
        t = EntityHelper.createRootEntity(rootType, entityId, tenantId);
        EntityHelper.updateTid(t, UUID.randomUUID().toString());
        return t;
    }

    protected  <R extends RootEntity, T extends ChildEntity<R>> T createChildEntity(String tenantId, Class<R> rootType, String rootId, Class<T> childType, String id) {
        checkTenantEligibility(tenantId);
        T t = (T) loadChildEntityByBrn(ChildEntity.makeRN(rootType, rootId, childType, id, tenantId));
        if (t != null) {
            throw new DomainEventException("root entity {0} already exist", t.getRN());
        }
        t = EntityHelper.createChildEntity(rootType, rootId, childType, id, tenantId);
        EntityHelper.updateTid(t, UUID.randomUUID().toString());
        return t;
    }

    protected abstract T loadRootEntityByBrn(String brn);

    protected abstract <R extends ChildEntity<T>> R loadChildEntityByBrn(String brn);

    private void checkTenantEligibility(String tenantId) {
        if (EntityHelper.hasTenant(rootType) && tenantId == null) {
            throw new DomainEventException("tenantId is null for entity creation");
        } else if (!EntityHelper.hasTenant(rootType) && tenantId != null) {
            throw new DomainEventException("tenantId is not applicable for entity creation");
        }
    }

    private <T extends Entity, E extends Event> T publish(E event) {

        if (!event.isRootEvent() && !isRootExist(makeRn(event.getRootEntityRn()))) {
            throw CollectionException.ROOT_DOESNT_EXIST(err -> err.setEntity(event.getRootOwner().getSimpleName()).setId(event.rootEntityId()));
        }
        Optional<T> entity = getEntity(makeRn(event.getEntityRn()), event);
        T e = entity.orElse(Entity.createEntity(event.getOwner(), event)).apply(event);
        mapStableCache.put(makeRn(e.getRN()), new EntitySnapshot(e, event.getSeqNum(), true));
        eventStream.publish(event);
        return e;
    }

    private <T extends Entity> Optional<T> getEntity(String id, Event event) {
        return Optional.ofNullable(mapStableCache.get(id, i -> loadSnapshot(id).orElse(null))).map(sp -> sp.getEntity());
    }

    protected abstract Optional<EntitySnapshot> loadSnapshot(String id);

    protected abstract void onEvent(Event event);

    protected abstract void insertEntity(Entity entity);

    protected abstract boolean isRootExist(String id);

    public abstract void startTransaction();

    public abstract void endTransaction();

    private String makeRn(String id) {
        return MessageFormat.format("{0}:{1}", rsPrefix, id);
    }

    public String getRsPrefix() {
        return rsPrefix;
    }

}
