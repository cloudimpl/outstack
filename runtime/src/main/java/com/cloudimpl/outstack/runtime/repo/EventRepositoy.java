/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.collection.error.CollectionException;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author nuwan
 */
public abstract class EventRepositoy {
    
    private final String rsPrefix;
    private final EventStream eventStream;
    private final Map<String,Entity> mapStableCache;
    public EventRepositoy(String rsPrefix,EventStream eventStream) {
        this.rsPrefix = rsPrefix;
        this.mapStableCache = new ConcurrentHashMap<>();
        this.eventStream = eventStream;
        this.eventStream.flux().publishOn(Schedulers.parallel()).doOnNext(this::onEvent).subscribe();
    }
    
    public <T extends Entity, E extends Event> T publish(E event) {
        Optional<T> entity = getEntity(makeRn(event.getEntityRn()));
        if (!event.isRootEvent() && !isRootExist(makeRn(event.getRootEntityRn()))) {
            throw CollectionException.ROOT_DOESNT_EXIST(err -> err.setEntity(event.getRootOwner().getSimpleName()).setId(event.rootEntityId()));
        }
        T e = entity.orElse(Entity.createEntity(event.getOwner(), event)).apply(event);
        eventStream.publish(event);
        return e;
    }
    
    protected abstract <T extends Entity> Optional<T> getEntity(String id);
    
    protected abstract void insertEntity(Entity entity);
    
    protected abstract boolean isRootExist(String id);

    private String makeRn(String id) {
        return MessageFormat.format("{0}:{1}", rsPrefix, id);
    }
    
    protected  void onEvent(Event event)
    {
        
    }
    <T extends Entity> T insert(T entity);
    
    <T extends Entity> T update(T entity);
    
    <T extends Entity> T deleteById(String entityType, String id);
    
    <T extends Entity> T deleteByEntityId(String entityType, String entityId);
}
