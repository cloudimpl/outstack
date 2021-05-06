/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class AyncEntityContext<T extends RootEntity> extends RootEntityContext<T>{
    
    public AyncEntityContext(Class<T> entityType, String tid, String tenantId, Function<String, ? extends Entity> entitySupplier, Supplier<String> idGenerator, ResourceHelper resourceHelper, CRUDOperations crudOperations, QueryOperations queryOperation, Consumer<Event> eventPublisher) {
        super(entityType, tid, tenantId, entitySupplier, idGenerator, resourceHelper, crudOperations, queryOperation, eventPublisher);
    }
    
    
    public <C extends ChildEntity<T>> C create(Class<C> type,String id, Event<C> event)
    {
        EntityContextProvider.Transaction<T> tx = getTx();
       // ChildEntityContext<T,C> context = tx.getContext((Class<K>) type);
        return null;
    }
}
