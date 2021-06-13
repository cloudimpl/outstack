/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author nuwan
 * @param <T>
 */
public abstract class EventRepositoy<T extends RootEntity> implements QueryOperations<T> {

    public static final String TID_PREFIX = "id-";
    protected final Class<T> rootType;
    private final EventStream eventStream;
    private final ResourceCache<? extends Entity> mapStableCache;
    private final ResourceCache<EntityCheckpoint> mapTxCheckpoints;
    protected final ResourceHelper resourceHelper;
    protected final String version;

    public EventRepositoy(Class<T> rootType, ResourceHelper resourceHelper, EventStream eventStream) {
        this.rootType = rootType;
        this.version = Entity.getVersion(rootType);
        this.resourceHelper = resourceHelper;
        this.mapStableCache = new ResourceCache<>(1000, Duration.ofHours(1));
        this.mapTxCheckpoints = new ResourceCache<>(1000, Duration.ofHours(1));
        this.eventStream = eventStream;
        //this.eventStream.flux().publishOn(Schedulers.parallel()).doOnNext(this::onEvent).subscribe();
    }

    public void saveTx(EntityContextProvider.Transaction transaction) {
        List<Event> events = transaction.getEventList();

        startTransaction();
        for (Event event : events) {
            System.out.println("tx: " + event);
            Entity e = applyEvent(event);
            System.out.println("entity: " + e + " event : " + event);
        }
        endTransaction();
    }

    public <T extends Entity> T applyEvent(Event event) {
        Entity e = null;
        switch (event.getAction()) {
            case CREATE: {
                e = createEntity(event);
                break;
            }
            case UPDATE: {
                e = updateEntity(event);
                break;
            }
            case DELETE: {
                deleteEntity(event);
                break;
            }
            case RENAME: {
                EntityRenamed renamedEvent = (EntityRenamed) event;
                renameEntity(renamedEvent);
                break;
            }
        }
        System.out.println("entity: " + e);
        System.out.println("event: " + event);
        long nextSeq = getCheckpoint(event.getRootEntityTRN()).getSeq() + 1;
        event.setSeqNum(nextSeq);
        getCheckpoint(event.getRootEntityTRN()).setSeq(nextSeq);
        addEvent(event);
        return (T) e;
    }

    private Entity createEntity(Event event) {
        Entity e;
        if (event.isRootEvent()) {
            e = RootEntity.create(event.getOwner(), event.entityId(), event.tenantId(), event.id());
            e.applyEvent(event);
            EntityHelper.setCreatedDate(e, event.getMeta().createdDate());
            EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
            saveRootEntityBrnIfNotExist(e);
            saveRootEntityTrnIfNotExist(e);
        } else {
            RootEntity root = (RootEntity) getRootById(event.getRootOwner(), event.id(), event.tenantId()).get();
            e = root.createChildEntity(event.getOwner(), event.entityId(), event.id());
            e.applyEvent(event);
            EntityHelper.setCreatedDate(e, event.getMeta().createdDate());
            EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
            saveChildEntityBrnIfNotExist(e);
            saveChildEntityTrnIfNotExist(e);
        }
        //      mapEntites.put(resourceHelper.getFQTrn(e), e);
        //      mapEntites.put(resourceHelper.getFQBrn(e), e);
        return e;
    }

    private Entity updateEntity(Event event) {

        Entity e;
        if (event.isRootEvent()) {
            e = (RootEntity) getRootById(event.getRootOwner(), event.id(), event.tenantId()).get();
            EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
            e.applyEvent(event);
            saveRootEntityBrnIfExist(e);
            saveRootEntityTrnIfExist(e);
        } else {
            e = (ChildEntity) getChildById(event.getRootOwner(), event.rootId(), event.getOwner(), event.id(), event.tenantId()).get();
            EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
            e.applyEvent(event);
            saveChildEntityBrnIfExist(e);
            saveChildEntityTrnIfExist(e);
        }
        return e;
    }

    private void deleteEntity(Event event) {
        if (event.isRootEvent()) {
            deleteRootEntityBrnById(event.getRootOwner(), event.entityId(), event.tenantId());
        } else {
            deleteChildEntityBrnById(event.getRootOwner(), event.rootId(), event.getOwner(), event.entityId(),event.tenantId());
        }
    }

    private void renameEntity(EntityRenamed event) {
        if (event.isRootEvent()) {
            deleteRootEntityBrnById(event.getOwner(), event.getOldEntityId(), event.tenantId());
            Entity e = (RootEntity) getRootById(event.getRootOwner(), event.id(), event.tenantId()).get();
            e = e.rename(event.entityId());
            saveRootEntityTrnIfExist(e);
            saveRootEntityBrnIfNotExist(e);
        } else {
            deleteChildEntityBrnById(event.getRootOwner(), event.rootId(), event.getOwner(), event.getOldEntityId(),event.tenantId());
            Entity e = (RootEntity) getChildById(event.getRootOwner(), event.rootId(),event.getOwner(),event.id(), event.tenantId()).get();
            e = e.rename(event.entityId());
            saveChildEntityTrnIfExist(e);
            saveChildEntityBrnIfNotExist(e);
        }
    }

    protected abstract void startTransaction();

    protected abstract void endTransaction();

    protected abstract <C extends ChildEntity<T>> Collection<C> getAllChildByType(Class<T> rootType,String id,Class<C> childType);
    
    protected abstract void saveRootEntityBrnIfNotExist(Entity e);

    protected abstract void saveRootEntityTrnIfNotExist(Entity e);

    protected abstract void saveRootEntityBrnIfExist(Entity e);

    protected abstract void saveRootEntityTrnIfExist(Entity e);

    protected abstract void saveChildEntityBrnIfNotExist(Entity e);

    protected abstract void saveChildEntityTrnIfNotExist(Entity e);

    protected abstract void saveChildEntityBrnIfExist(Entity e);

    protected abstract void saveChildEntityTrnIfExist(Entity e);

    protected abstract void deleteRootEntityBrnById(Class<T> rootType, String id, String tenantId);

    protected abstract void deleteRootEntityTrnById(Class<T> rootType, String id, String tenantId);

    protected abstract <C extends ChildEntity<T>> void deleteChildEntityBrnById(Class<T> rootType, String id, Class<C> childType, String childId,String tenantId);

    protected abstract <C extends ChildEntity<T>> void deleteChildEntityTrnById(Class<T> rootType, String id, Class<C> childType, String childId,String tenantId);

    protected abstract EntityCheckpoint getCheckpoint(String rootTrn);

    protected abstract void addEvent(Event event);
    
    public String generateTid() {
        return TID_PREFIX + UUID.randomUUID().toString();
    }

//    public <K extends Entity> K loadEntityWithClone(String resourceName) {
//         return (K) loadEntity(resourceName).map(e->e.cloneEntity()).orElse(null);
//    }

    public <K extends Entity, C extends ChildEntity<T>> Optional<K> loadEntityWithClone(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        if (childType == null) {
            return getRootById(rootType, id, tenantId).map(e -> e.cloneEntity());
        } else {
            return getChildById(rootType, id, childType, childId, tenantId).map(e -> e.cloneEntity());
        }
    }
}
