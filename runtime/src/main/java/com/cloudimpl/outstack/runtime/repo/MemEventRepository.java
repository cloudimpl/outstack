/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.domain.v1.ChildEntity;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 *
 * @author nuwan
 */
public class MemEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final Map<String, EntitySnapshot> snapshots = new ConcurrentHashMap<>();
    private final Map<String, Entity> entityByTid = new ConcurrentHashMap<>();
    private final Map<String, Entity> entityById = new ConcurrentHashMap<>();
    private final TreeMap<String,Event> events = new TreeMap<>();
    
    @Override
    protected Optional<EntitySnapshot> loadSnapshot(String id) {
        return Optional.ofNullable(snapshots.get(id));
    }
 
    @Override
    protected synchronized void createRootEntity(RootEntity entity) {
        EntityHelper.updateTid(entity, UUID.randomUUID().toString());
        Entity old = entityById.putIfAbsent(entity.getRN(), entity);
        if (old != null) {
            throw new DomainEventException("entity already exist: {0}:{1}",entity.getClass().getSimpleName(),entity.id());
        }
        old = entityByTid.putIfAbsent(entity.tid(), entity);
        while(old != null)
        {
            EntityHelper.updateTid(entity, UUID.randomUUID().toString());
            old = entityByTid.putIfAbsent(entity.tid(), entity);
        }     
    }

    private String makeKey(Entity entity)
    {
        return MessageFormat.format("{0}:{1}:{2}", getClass(),entity.getClass().getSimpleName(),entity.);
    }
    
    @Override
    protected synchronized void createChildEntity(ChildEntity entity) {
        EntityHelper.updateTid(entity, UUID.randomUUID().toString());
        Entity old = entityById.putIfAbsent(entity.id(), entity);
        if (old != null) {
            throw new DomainEventException("entity already exist: {0}:{1}",entity.getClass().getSimpleName(),entity.id());
        }
        old = entityByTid.putIfAbsent(entity.tid(), entity);
        while(old != null)
        {
            EntityHelper.updateTid(entity, UUID.randomUUID().toString());
            old = entityByTid.putIfAbsent(entity.tid(), entity);
        }
       
    }
    
    @Override
    protected synchronized void onEvent(Event event) {
        events.put(event.getRootEntityRn()+"/"+event.getSeqNum(), event);
    }

    @Override
    protected void insertEntity(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean isRootExist(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
