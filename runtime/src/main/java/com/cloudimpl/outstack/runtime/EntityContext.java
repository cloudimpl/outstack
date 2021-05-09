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
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author nuwansa
 * @param <T>
 */
public abstract class EntityContext<T extends Entity> implements Context {

    private final String tenantId;
    protected final Class<T> entityType;
    private final List<Event> events;
    private final EntityProvider<?> entitySupplier;
    protected final Supplier<String> idGenerator;
    protected final CRUDOperations crudOperations;
    private final QueryOperations<?> queryOperation;
    protected final Consumer<Event> eventPublisher;
    protected EntityContextProvider.Transaction tx;
    protected final Consumer<Object> validator;
    public EntityContext(Class<T> entityType, String tenantId, EntityProvider<?> entitySupplier,Supplier<String> idGenerator,CRUDOperations crudOperations,QueryOperations<?> queryOperation,Consumer<Event> eventPublisher,Consumer<Object> validator) {
        this.tenantId = tenantId;
        this.events = new LinkedList<>();
        this.entityType = entityType;
        this.entitySupplier = entitySupplier;
        this.idGenerator = idGenerator;
        this.crudOperations = crudOperations;
        this.eventPublisher = eventPublisher;
        this.queryOperation = queryOperation;
        this.validator = validator;
    }

    protected EntityContextProvider.Transaction getTx()
    {
        return this.tx;
    }

    protected <R extends RootEntity> EntityProvider<R> getEntityProvider()
    {
        return (EntityProvider<R>) entitySupplier;
    }
    
    protected <R extends RootEntity> QueryOperations<R> getQueryOperations()
    {
        return (QueryOperations<R>) queryOperation;
    }
    
    public void setTx(EntityContextProvider.Transaction tx) {
        this.tx = tx;
    }
   
    public String getTenantId() {
        return tenantId;
    }

    public List<Event> getEvents() {
        return this.events;
    }

    protected void addEvent(Event<T> event)
    {
        this.events.add(event);
    }
    public abstract T create(String id,Event<T> event);
    public abstract T update(String id,Event<T> event);
    public abstract T delete(String id);
    public abstract T rename(String id,String newId);
    
    public abstract <R extends RootEntity> RootEntityContext<R> asRootContext();


    public abstract <R extends RootEntity,K extends ChildEntity<R>> ChildEntityContext<R,K> asChildContext() ;
}
