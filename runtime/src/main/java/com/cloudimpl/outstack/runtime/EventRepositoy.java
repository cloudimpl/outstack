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
import java.text.MessageFormat;
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
        EntityCheckpoint checkpoint = getCheckpoint(event.getRootEntityTRN());
        long nextSeq = checkpoint.getSeq() + 1;
        event.setSeqNum(nextSeq);
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
        event.setSeqNum(nextSeq);
        checkpoint.setSeq(nextSeq);
        addEvent(event);
        updateCheckpoint(nextSeq - 1, checkpoint);
        return (T) e;
    }

    private Entity createEntity(Event event) {
        Entity e;
        if (event.isRootEvent()) {
            RootEntity root = RootEntity.create(event.getOwner(), event.entityId(), event.tenantId(), event.id());
            root.applyEvent(event);
            EntityHelper.setCreatedDate(root, event.getMeta().createdDate());
            EntityHelper.setLastEq(root, event.getSeqNum());
            EntityHelper.setUpdatedDate(root, event.getMeta().createdDate());
            saveRootEntityBrnIfNotExist(root);
            saveRootEntityTrnIfNotExist(root);
            e = root;
        } else {
            RootEntity root = (RootEntity) getRootById(event.getRootOwner(), event.id(), event.tenantId()).get();
            ChildEntity child = root.createChildEntity(event.getOwner(), event.entityId(), event.id());
            child.applyEvent(event);
            EntityHelper.setCreatedDate(child, event.getMeta().createdDate());
            EntityHelper.setLastEq(child, event.getSeqNum());
            EntityHelper.setUpdatedDate(child, event.getMeta().createdDate());
            saveChildEntityBrnIfNotExist(event.getRootEntityTRN(), child);
            saveChildEntityTrnIfNotExist(event.getRootEntityTRN(), child);
            e = child;
        }
        //      mapEntites.put(resourceHelper.getFQTrn(e), e);
        //      mapEntites.put(resourceHelper.getFQBrn(e), e);
        return e;
    }

    private Entity updateEntity(Event event) {

        Entity e;
        if (event.isRootEvent()) {
            RootEntity root = (RootEntity) getRootById(event.getRootOwner(), event.id(), event.tenantId()).get();
            EntityHelper.setUpdatedDate(root, event.getMeta().createdDate());
            long lastSeq = root.getMeta().getLastSeq();
            EntityHelper.setLastEq(root, event.getSeqNum());
            root.applyEvent(event);
            saveRootEntityBrnIfExist(lastSeq, root);
            saveRootEntityTrnIfExist(lastSeq, root);
            e = root;
        } else {
            ChildEntity child = (ChildEntity) getChildById(event.getRootOwner(), event.rootId(), event.getOwner(), event.id(), event.tenantId()).get();
            EntityHelper.setUpdatedDate(child, event.getMeta().createdDate());
            long lastSeq = child.getMeta().getLastSeq();
            EntityHelper.setLastEq(child, event.getSeqNum());
            child.applyEvent(event);
            saveChildEntityBrnIfExist(lastSeq, event.getRootEntityTRN(), child);
            saveChildEntityTrnIfExist(lastSeq, event.getRootEntityTRN(), child);
            e = child;
        }
        return e;
    }

    private void deleteEntity(Event event) {
        if (event.isRootEvent()) {
            deleteRootEntityBrnById(event.getRootOwner(), event.entityId(), event.tenantId());
        } else {
            deleteChildEntityBrnById(event.getRootOwner(), event.rootId(), event.getOwner(), event.entityId(), event.tenantId());
        }
    }

    private void renameEntity(EntityRenamed event) {
        if (event.isRootEvent()) {
            deleteRootEntityBrnById(event.getOwner(), event.getOldEntityId(), event.tenantId());
            RootEntity e = (RootEntity) getRootById(event.getRootOwner(), event.id(), event.tenantId()).get();
            long lastSeq = e.getMeta().getLastSeq();
            e = e.rename(event.entityId());
            EntityHelper.setLastEq(e, event.getSeqNum());
            saveRootEntityTrnIfExist(lastSeq, e);
            saveRootEntityBrnIfNotExist(e);
        } else {
            deleteChildEntityBrnById(event.getRootOwner(), event.rootId(), event.getOwner(), event.getOldEntityId(), event.tenantId());
            ChildEntity e = (ChildEntity) getChildById(event.getRootOwner(), event.rootId(), event.getOwner(), event.id(), event.tenantId()).get();
            long lastSeq = e.getMeta().getLastSeq();
            e = e.rename(event.entityId());
            EntityHelper.setLastEq(e, event.getSeqNum());
            saveChildEntityTrnIfExist(lastSeq, event.getRootEntityTRN(), e);
            saveChildEntityBrnIfNotExist(event.getRootEntityTRN(), e);
        }
    }

    protected abstract void startTransaction();

    protected abstract void endTransaction();

    protected abstract <C extends ChildEntity<T>> Collection<C> getAllChildByType(Class<T> rootType, String id, Class<C> childType);

    protected abstract void saveRootEntityBrnIfNotExist(RootEntity e);

    protected abstract void saveRootEntityTrnIfNotExist(RootEntity e);

    protected abstract void saveRootEntityBrnIfExist(long lastSeq, RootEntity e);

    protected abstract void saveRootEntityTrnIfExist(long lastSeq, RootEntity e);

    protected abstract void saveChildEntityBrnIfNotExist(String rootTrn, ChildEntity e);

    protected abstract void saveChildEntityTrnIfNotExist(String rootTrn, ChildEntity e);

    protected abstract void saveChildEntityBrnIfExist(long lastSeq, String rootTrn, ChildEntity e);

    protected abstract void saveChildEntityTrnIfExist(long lastSeq, String rootTrn, ChildEntity e);

    protected abstract void deleteRootEntityBrnById(Class<T> rootType, String id, String tenantId);

    protected abstract void deleteRootEntityTrnById(Class<T> rootType, String id, String tenantId);

    protected abstract <C extends ChildEntity<T>> void deleteChildEntityBrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId);

    protected abstract <C extends ChildEntity<T>> void deleteChildEntityTrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId);

    protected abstract EntityCheckpoint getCheckpoint(String rootTrn);

    protected abstract void updateCheckpoint(long lastSeq, EntityCheckpoint checkpoint);

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

    protected String resourcePrefix(String prefix) {
        return MessageFormat.format("{0}:{1}", prefix, resourceHelper);
    }
}
