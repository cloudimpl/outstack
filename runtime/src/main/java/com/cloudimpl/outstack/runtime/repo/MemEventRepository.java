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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class MemEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final TreeMap<String, Entity> mapEntites = new TreeMap<>();

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
                    mapEntites.put(transaction.getEntityBrn(e.getBRN()), e);
                    mapEntites.put(transaction.getEntityTrn(e.getTRN()), e);
                    System.out.println("interting brn:"+transaction.getEntityBrn(e.getBRN()));
                    System.out.println("interting trn:"+transaction.getEntityTrn(e.getBRN()));
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
                    mapEntites.put(transaction.getEntityTrn(event), e);
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
            RootEntity root = (RootEntity) mapEntites.get(rootFqTrd);
            e = root.createChildEntity(event.getOwner(), event.entityId(), event.id());
            e.applyEvent(event); 
        }
        return e;
    }

    private Entity updateEntity(String fqTrd, Event event) {
        Entity e;
        if (event.isRootEvent()) {
            e = mapEntites.get(fqTrd);
            e.applyEvent(event);
        } else {
            e = mapEntites.get(fqTrd);
            e.applyEvent(event);
        }
        return e;
    }

    private Entity deleteEntity(String fqBrn) {
        return mapEntites.remove(fqBrn);
    }

    private Entity renamEntity(String oldBrn, String newBrn, Event event) {
        Entity e = mapEntites.get(oldBrn);
        e = e.rename(event.entityId());
        mapEntites.remove(oldBrn);
        mapEntites.put(newBrn, e);
        return e;
    }

    @Override
    protected synchronized <K extends Entity> Optional<K> loadEntityByBrn(String resourceName) {
        System.out.println("loading brn: "+resourceName);
        return Optional.ofNullable((K) mapEntites.get(resourceName));
    }

    @Override
    protected synchronized <K extends Entity> Optional<K> loadEntityByTrn(String resourceName) {
        System.out.println("loading trn: "+resourceName);
        return Optional.ofNullable((K) mapEntites.get(resourceName));
    }

    @Override
    public <T extends RootEntity> Optional<T> getRootById(String rn) {
        return Optional.ofNullable((T) mapEntites.get(rn));
    }

    @Override
    public <R extends RootEntity, T extends ChildEntity<R>> Optional<T> getChildById(String rn) {
       return Optional.ofNullable((T) mapEntites.get(rn));
    }

    @Override
    public <R extends RootEntity, T extends ChildEntity<R>> Collection<T> getAllChildByType(String rootTrn,Class<T> childType) {
       return mapEntites.headMap(rootTrn+"/"+childType.getSimpleName()).values().stream().map(e->(T)e).collect(Collectors.toList());
    }

}
