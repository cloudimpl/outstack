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
 * @param <T>
 */
public class RootEntityContext<T extends RootEntity> extends EntityContext<T> implements RootEntityQueryContext<T>{

    private String _id;

    public RootEntityContext(Class<T> entityType,String tid, String tenantId, Function<String, ? extends Entity> entitySupplier, Supplier<String> idGenerator, ResourceHelper resourceHelper, CRUDOperations crudOperations,QueryOperations queryOperation, Consumer<Event> eventPublisher) {
        super(entityType, tenantId, entitySupplier, idGenerator, resourceHelper, crudOperations,queryOperation, eventPublisher);
        this._id = tid;
    }

    @Override
     public  RootEntityContext<T> asRootContext()
     {
         return this;
     }
     
    @Override
    public T create(String id, Event<T> event) {
        EntityIdHelper.validateEntityId(id);
        Objects.requireNonNull(event);
 
        if(_id != null)
        {
            throw new DomainEventException("rootId violation.");
        }
        T root = (T) entitySupplier.apply(resourceHelper.getFQBrn(RootEntity.makeRN(entityType, id, getTenantId())));
        if (root != null) {
            throw new DomainEventException("root entity {0} already exist", root.getBRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        root = RootEntity.create(entityType, id, getTenantId(), idGenerator.get());

        event.setTenantId(getTenantId());
        event.setId(root.id());
        event.setRootId(root.id());
        event.setAction(Event.Action.CREATE);
        root.applyEvent(event);
        addEvent(event);
        this._id = root.id();
        crudOperations.create(root);
        eventPublisher.accept(event);
        return root;
    }

    @Override
    public T update(String id, Event<T> event) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(event);
        if (_id == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }
        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this._id, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity not available for entity {0}", entityType.getSimpleName());
        }
        if (!root.entityId().equals(id)) {
            throw new DomainEventException("update failed,invalid id {0} for root entity {1}", id, root.getBRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        event.setTenantId(getTenantId());
        event.setId(_id);
        event.setRootId(_id);
        event.setAction(Event.Action.UPDATE);
        root.applyEvent(event);
        addEvent(event);
        crudOperations.update(root);
        eventPublisher.accept(event);
        return root;
    }

    @Override
    public T delete(String id) {
        Objects.requireNonNull(id);
        if (_id == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }

        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this._id, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity id {0} not available for entity {1}", id, entityType.getSimpleName());
        }
        if (!root.entityId().equals(id)) {
            throw new DomainEventException("invalid id {0} for root entity {1}", id, root.getBRN());
        }

        EntityDeleted event = new EntityDeleted(entityType, entityType, root.entityId(), root.entityId());
        event.setId(_id);
        event.setRootId(_id);
        event.setTenantId(getTenantId());
        event.setAction(Event.Action.DELETE);
        addEvent(event);
        crudOperations.delete(root);
        eventPublisher.accept(event);
        return root;
    }

    @Override
    public T rename(String id, String newId) {
        EntityIdHelper.validateEntityId(id);
        EntityIdHelper.validateEntityId(newId);
 
        if (_id == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }

        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this._id, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity id {0} not available for entity {1}", id, entityType.getSimpleName());
        }
        if (!root.entityId().equals(id)) {
            throw new DomainEventException("invalid id {0} for root entity {1}", id, root.getBRN());
        }
       
        EntityRenamed event = new EntityRenamed(entityType, entityType, newId, id, newId);
        event.setTenantId(getTenantId());
        event.setId(_id);
        event.setRootId(_id);
        event.setAction(Event.Action.RENAME);
        addEvent(event);
        root = root.rename(newId);
        crudOperations.rename(id, root);
        eventPublisher.accept(event);
        return root;
    }

    @Override
    public  ChildEntityContext asChildContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Optional<T> getByEntityId(String id)
    {
        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeRN(entityType, id, getTenantId())));
        return Optional.ofNullable(root);
    }
    
    @Override
    public Optional<T> getById(String id)
    {
        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, id, getTenantId())));
        return Optional.ofNullable(root);
    }
    
    public <C extends ChildEntity<T>>  Optional<C> getChildByEntityId(Class<C> childType,String id)
    {
        return queryOperation.getChildById(resourceHelper.getFQBrn(ChildEntity.makeRN(entityType,_id,childType,id, getTenantId())));
    }
    
    public <C extends ChildEntity<T>>  Optional<C> getChildById(Class<C> childType,String id)
    {
       return queryOperation.getChildById(resourceHelper.getFQTrn(ChildEntity.makeTRN(entityType,_id,childType,id, getTenantId())));
    }
     
    public <C extends ChildEntity<T>> Collection<C> getAllChildsByType(Class<C> childType)
    {
        return queryOperation.getAllChildByType(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType,_id,getTenantId())),childType);
    }

    @Override
    public RootEntityQueryContext<T> asRootQueryContext() {
       return this;
    }

    @Override
    public <R extends RootEntity, K extends ChildEntity<R>> ChildEntityQueryContext<R, K> asChildQueryContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<T> getRoot() {
        if(this._id == null)
            return Optional.empty();
        return Optional.ofNullable((T)entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this._id, getTenantId()))));
    }
}
