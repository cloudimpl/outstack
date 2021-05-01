/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nuwan
 * @param <T>
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
            System.out.println("tx: "+event);
            Entity e = null;
            switch (event.getAction()) {
                case CREATE: {
                    e = createEntity(transaction.getEntityTrn(event.getRootEntityTRN()),transaction.getEntityTrn(event), event);
                    mapEntitiesByBrn.put(transaction.getEntityBrn(event), e);
                    mapEntitiesByTrn.put(transaction.getEntityTrn(event), e);
                    break;  
                }
                case UPDATE: {
                    e = updateEntity(transaction.getEntityTrn(event), event);
                    break;
                }
                case DELETE: {
                    e = deleteEntity(transaction.getEntityBrn(event));
                    break;
                }
                case RENAME: {
                    EntityRenamed renamedEvent = (EntityRenamed) event;
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
            System.out.println("entity: "+e);
        }
    }

    private Entity createEntity(String rootFqTrd,String fqTrd, Event event) {
        Entity e;
        if (event.isRootEvent()) {
            e = RootEntity.create(event.getOwner(), event.entityId(), event.tenantId(), event.id());
            e.applyEvent(event);
        } else {
            RootEntity root = (RootEntity) mapEntitiesByTrn.get(rootFqTrd);
            e = root.createChildEntity(event.getOwner(), event.entityId(), event.id());
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

    private Entity deleteEntity(String fqBrn) {
        return mapEntitiesByBrn.remove(fqBrn);
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
        return Optional.ofNullable((K) mapEntitiesByBrn.get(resourceName));
    }

    @Override
    protected <K extends Entity> Optional<K> loadEntityByTrn(String resourceName) {
        return Optional.ofNullable((K) mapEntitiesByTrn.get(resourceName));
    }

}
