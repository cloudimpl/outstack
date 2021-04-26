/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.domain.v1.ChildEntity;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.EntityRenamed;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nuwan
 */
public class MemEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final Map<String, Entity> mapEntitiesByTrn = new ConcurrentHashMap<>();
    private final Map<String, Entity> mapEntitiesByBrn = new ConcurrentHashMap<>();

    public MemEventRepository(Class<T> rootType, EventStream eventStream) {
        super(rootType, eventStream);
    }

    @Override
    public synchronized void saveTx(EntityContextProvider.Transaction transaction) {
        List<Event> events = transaction.getEventList();
        for (Event event : events) {
            switch (event.getAction()) {
                case CREATE: {
                    Entity e = createEntity(transaction.getEntityTrn(event), event);
                    mapEntitiesByBrn.put(transaction.getEntityBrn(event), e);
                    mapEntitiesByTrn.put(transaction.getEntityTrn(event), e);
                    break;
                }
                case UPDATE: {
                    Entity e = updateEntity(transaction.getEntityTrn(event), event);
                    break;
                }
                case DELETE: {
                    deleteEntity(transaction.getEntityBrn(event));
                    break;
                }
                case RENAME: {
                    EntityRenamed renamedEvent = (EntityRenamed) event;
                    Entity e;
                    if (event.isRootEvent()) {
                        e = renamEntity(transaction.getEntityBrn(RootEntity.makeRN(event.getOwner(), renamedEvent.getOldEntityId(), event.tenantId())),
                                 transaction.getEntityBrn(RootEntity.makeRN(event.getOwner(), renamedEvent.entityId(), event.tenantId())),
                                 event);
                    } else {
                        e = renamEntity(transaction.getEntityBrn(ChildEntity.makeRN(event.getRootOwner(), event.rootEntityId(), event.getOwner(), renamedEvent.getOldEntityId(), event.tenantId())),
                                 transaction.getEntityBrn(ChildEntity.makeRN(event.getRootOwner(), event.rootEntityId(), event.getOwner(), renamedEvent.entityId(), event.tenantId())),
                                 event);
                    }
                    mapEntitiesByTrn.put(transaction.getEntityTrn(event), e);
                    break;
                }
            }
        }
    }

    private Entity createEntity(String fqTrd, Event event) {
        Entity e;
        if (event.isRootEvent()) {
            e = RootEntity.create(event.getOwner(), event.entityId(), event.tenantId(), event.tid());
            e.applyEvent(event);
        } else {
            RootEntity root = (RootEntity) mapEntitiesByTrn.get(fqTrd);
            e = root.createChildEntity(event.getOwner(), event.entityId(), event.tid());
            e.applyEvent(event);
        }
        return e;
    }

    private Entity updateEntity(String fqTrd, Event event) {
        Entity e;
        if (event.isRootEvent()) {
            e = mapEntitiesByTrn.get(fqTrd);
            e.applyEvent(event);
        } else {
            e = mapEntitiesByTrn.get(fqTrd);
            e.applyEvent(event);
        }
        return e;
    }

    private void deleteEntity(String fqBrn) {
        mapEntitiesByBrn.remove(fqBrn);
    }

    private Entity renamEntity(String oldBrn, String newBrn, Event event) {
        Entity e = mapEntitiesByBrn.get(oldBrn);
        e = e.rename(event.entityId());
        mapEntitiesByBrn.remove(oldBrn);
        mapEntitiesByBrn.put(newBrn, e);
        return e;
    }

    @Override
    protected <K extends Entity> Optional<K> loadEntityByBrn(String resourceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected <K extends Entity> Optional<K> loadEntityByTrn(String resourceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
