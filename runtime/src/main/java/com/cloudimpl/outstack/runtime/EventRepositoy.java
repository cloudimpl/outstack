/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.RepositoryException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author nuwan
 * @param <T>
 */
public abstract class EventRepositoy<T extends RootEntity> implements QueryOperations<T>{

    public static final String TID_PREFIX = "id-";
    private final Class<T> rootType;
    private final EventStream eventStream;
    private final ResourceCache<? extends Entity> mapStableCache;
    private final ResourceCache<TxCheckpoint> mapTxCheckpoints;
    protected final ResourceHelper resourceHelper;
    public EventRepositoy( Class<T> rootType,ResourceHelper resourceHelper, EventStream eventStream) {
        this.rootType = rootType;
        this.resourceHelper = resourceHelper;
        this.mapStableCache = new ResourceCache<>(1000, Duration.ofHours(1));
        this.mapTxCheckpoints = new ResourceCache<>(1000,Duration.ofHours(1));
        this.eventStream = eventStream;
        //this.eventStream.flux().publishOn(Schedulers.parallel()).doOnNext(this::onEvent).subscribe();
    }

//    public void publish(EventTxList txList) {
//        txList.getEvents().forEach(this::publish);
//    }
    public String generateTid() {
        return TID_PREFIX+UUID.randomUUID().toString();
    }

    public abstract void saveTx(EntityContextProvider.Transaction transaction);
    
//    public <K extends Entity> K loadEntityWithClone(String resourceName) {
//         return (K) loadEntity(resourceName).map(e->e.cloneEntity()).orElse(null);
//    }
    
    public <K extends Entity,C extends ChildEntity<T>> Optional<K> loadEntityWithClone(Class<T> rootType,String id,Class<C> childType,String childId,String tenantId)
    {
        if(childType == null)
        {
            return getRootById(rootType, id, tenantId).map(e->e.cloneEntity());
        }
        else
        {
            return getChildById(rootType, id, childType, childId, tenantId).map(e->e.cloneEntity());
        }
    }
}
