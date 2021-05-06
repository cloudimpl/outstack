/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityDeleted;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author nuwan
 * @param <R>
 * @param <T>
 */
public class ChildEntityContext<R extends RootEntity, T extends ChildEntity<R>> extends EntityContext<T> implements ChildEntityQueryContext<R, T>{

    private final Class<R> rootType;
    private final String rootId;
    
    public ChildEntityContext(Class<R> rootType, String rootId, Class<T> entityType, String tenantId, Function<String, ? extends Entity> entitySupplier, Supplier<String> idGenerator, ResourceHelper resourceHelper, CRUDOperations crudOperations,QueryOperations queryOperation, Consumer<Event> eventPublisher) {
        super(entityType, tenantId, entitySupplier, idGenerator, resourceHelper, crudOperations,queryOperation, eventPublisher);
        this.rootType = rootType;
        this.rootId = rootId;
        Objects.requireNonNull(this.rootId);
    }

    @Override
    public T create(String id, Event<T> event) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootId, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", event.getRootEntityRN());
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId())));
        if (child != null) {
            throw new DomainEventException("child entity {0} is already exist", child.getBRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        if(!root.entityId().equals(event.rootEntityId()))
        {
            throw new DomainEventException("root entity Id and event root id not equal. {0} , {1}", root.entityId(), event.rootEntityId());
        }
        
        child = root.createChildEntity(entityType, id, idGenerator.get());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.CREATE);
        child.applyEvent(event);
        addEvent(event);
        crudOperations.create(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T update(String id, Event<T> event) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootId, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", event.getRootEntityRN());
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.entityId(), entityType, id, getTenantId())));
        if (child == null) {
            throw new DomainEventException("child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.entityId(), entityType, id, getTenantId()));
        }
        if(!event.rootEntityId().equals(root.entityId()))
        {
            throw new DomainEventException("invalid root entity id {0} in the event faor root entity {1}", event.rootEntityId(),root.entityId());
        }
        if (!child.entityId().equals(id)) {
            throw new DomainEventException("invalid id {0} for child entity {1}", id, child.getBRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        child = root.createChildEntity(entityType, id, idGenerator.get());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.UPDATE);
        child.applyEvent(event);
        addEvent(event);
        crudOperations.update(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T delete(String id) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootId, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", RootEntity.makeTRN(rootType, rootId, getTenantId()));
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.entityId(), entityType, id, getTenantId())));
        if (child == null) {
            throw new DomainEventException("child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.entityId(), entityType, id, getTenantId()));
        }
        if (!root.entityId().equals(id)) {
            throw new DomainEventException("invalid id {0} for child entity {1}", id, child.getBRN());
        }
        EntityDeleted event = new EntityDeleted(entityType, rootType, child.entityId(), root.entityId());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.DELETE);
        addEvent(event);
        crudOperations.delete(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T rename(String id, String newId) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootId, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", RootEntity.makeTRN(rootType, rootId, getTenantId()));
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId())));
        if (child == null) {
            throw new DomainEventException("child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId()));
        }
        if (!root.entityId().equals(id)) {
            throw new DomainEventException("invalid id {0} for child entity {1}", id, child.getBRN());
        }
        EntityRenamed event = new EntityRenamed(entityType, rootType, newId, id, root.entityId());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.RENAME);
        addEvent(event);
        child = child.rename(newId);
        crudOperations.rename(id, child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public <R extends RootEntity> RootEntityContext<R> asRootContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChildEntityContext<R,T> asChildContext() {
        return this;
    }

    @Override
    public Optional<T> getByEntityId(String id)
    {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootId, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", RootEntity.makeTRN(rootType, rootId, getTenantId()));
        }
        return Optional.ofNullable((T)entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId()))));
    }
    
    
    @Override
    public Optional<T> getById(String id)
    {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootId, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", RootEntity.makeTRN(rootType, rootId, getTenantId()));
        }
        return Optional.ofNullable((T)entitySupplier.apply(resourceHelper.getFQTrn(ChildEntity.makeTRN(rootType, root.id(), entityType, id, getTenantId()))));
    }
    
    @Override
    public R getRoot()
    {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootId, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", RootEntity.makeTRN(rootType, rootId, getTenantId()));
        }
        return root;
    }
    
    @Override
    public Collection<T> getAllByEntityType(Class<T> type)
    {
         return queryOperation.getAllChildByType(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType,rootId,getTenantId())),type);
    }

    @Override
    public <R extends RootEntity> RootEntityQueryContext<R> asRootQueryContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChildEntityQueryContext<R, T> asChildQueryContext() {
        return  this;
    }
}
