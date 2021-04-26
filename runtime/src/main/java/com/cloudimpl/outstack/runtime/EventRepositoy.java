/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;
import com.cloudimpl.outstack.runtime.repo.RepositoryException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author nuwan
 * @param <T>
 */
public abstract class EventRepositoy<T extends RootEntity> {

    private final Class<T> rootType;
    private final EventStream eventStream;
    private final ResourceCache<? extends Entity> mapStableCache;
    private final ResourceCache<TxCheckpoint> mapTxCheckpoints;
    public EventRepositoy( Class<T> rootType, EventStream eventStream) {
        this.rootType = rootType;
        this.mapStableCache = new ResourceCache<>(1000, Duration.ofHours(1));
        this.mapTxCheckpoints = new ResourceCache<>(1000,Duration.ofHours(1));
        this.eventStream = eventStream;
        //this.eventStream.flux().publishOn(Schedulers.parallel()).doOnNext(this::onEvent).subscribe();
    }

//    public void publish(EventTxList txList) {
//        txList.getEvents().forEach(this::publish);
//    }
    public String generateTid() {
        return UUID.randomUUID().toString();
    }

    public abstract void saveTx(EntityContextProvider.Transaction transaction);
    
    public <K extends Entity> K loadEntityWithClone(String resourceName) {
         return (K) loadEntity(resourceName).map(e->e.cloneEntity()).orElse(null);
    }
    
    private <K extends Entity> Optional<K> loadEntity(String resourceName) {
        if (resourceName.startsWith("brn:")) {
            return mapStableCache.<K>get(resourceName).or(()->loadEntityByBrn(resourceName));
        } else if (resourceName.startsWith("trn:")) {
            return mapStableCache.<K>get(resourceName).or(()->loadEntityByTrn(resourceName));
        } else {
            throw new RepositoryException("uknown resource name {0}", resourceName);
        }
    }

    protected abstract <K extends Entity> Optional<K> loadEntityByBrn(String resourceName);

    protected abstract <K extends Entity> Optional<K> loadEntityByTrn(String resourceName);
}
