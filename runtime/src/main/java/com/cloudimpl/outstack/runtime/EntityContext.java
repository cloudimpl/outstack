/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.collection.error.CollectionException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
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
    protected final Function<String, ? extends Entity> entitySupplier;
    protected final Supplier<String> idGenerator;
    protected final ResourceHelper resourceHelper;
    protected final CRUDOpertations crudOperations;
    protected final Consumer<Event> eventPublisher;
    public EntityContext(Class<T> entityType, String tenantId, Function<String, ? extends Entity> entitySupplier,Supplier<String> idGenerator,ResourceHelper resourceHelper,CRUDOpertations crudOperations,Consumer<Event> eventPublisher) {
        this.tenantId = tenantId;
        this.events = new LinkedList<>();
        this.entityType = entityType;
        this.entitySupplier = entitySupplier;
        this.idGenerator = idGenerator;
        this.resourceHelper = resourceHelper;
        this.crudOperations = crudOperations;
        this.eventPublisher = eventPublisher;
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
    
}
