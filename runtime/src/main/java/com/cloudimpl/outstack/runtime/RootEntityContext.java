/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityDeleted;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
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
public class RootEntityContext<T extends RootEntity> extends EntityContext<T> implements RootEntityQueryContext<T> {

    private String _id;

    public RootEntityContext(Class<T> entityType, String tid, String tenantId, Optional<EntityProvider<? extends RootEntity>> entitySupplier,
            Supplier<String> idGenerator, Optional<CRUDOperations> crudOperations, QueryOperations<T> queryOperation,
            Optional<Consumer<Event>> eventPublisher,Consumer<Object> validator,
            Function<Class<? extends RootEntity> ,QueryOperations<?>> queryOperationSelector,String version) {
        super(entityType, tenantId, entitySupplier, idGenerator, crudOperations, queryOperation, eventPublisher,validator,queryOperationSelector,version);
        this._id = tid;
    }

    @Override
    public RootEntityContext<T> asRootContext() {
        return this;
    }

    @Override
    public T create(String id, Event<T> event) {
        EntityIdHelper.validateEntityId(id);
        Objects.requireNonNull(event);
        validator.accept(event);
        if (_id != null) {
            throw new DomainEventException(DomainEventException.ErrorCode.BASIC_VIOLATION,"rootId violation.");
        }
        this.<T>getEntityProvider().loadEntity(entityType, id, null, null, getTenantId())
                .ifPresent(e -> {
                    throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_EXIST,"root entity {0} already exist", ((T)e).getBRN());
                });

        if (!event.entityId().equals(id)) {
            throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_EVENT_RELATION_VIOLATION,"event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setVersion(event, version);
        T root = RootEntity.create(entityType, id, getTenantId(), idGenerator.get());

        event.setTenantId(getTenantId());
        event.setId(root.id());
        event.setRootId(root.id());
        event.setAction(Event.Action.CREATE);
        root.applyEvent(event);
        EntityHelper.setCreatedDate(root, event.getMeta().createdDate());
        EntityHelper.setUpdatedDate(root, event.getMeta().createdDate());
        validator.accept(root);
        addEvent(event);
        this._id = root.id();
        getCrudOperations().create(root);
        getEventPublisher().accept(event);
        return root;
    }

    @Override
    public T update(String id, Event<T> event) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(event);
        validator.accept(event);
        if (_id == null) {
            throw new DomainEventException(DomainEventException.ErrorCode.BASIC_VIOLATION,"root tid not available for entity {0}", entityType.getSimpleName());
        }
        T root = (T)this.<T>getEntityProvider().loadEntity(entityType, id, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity not available for entity {0}", entityType.getSimpleName()));   
        
        EntityIdHelper.validateId(id, root);
        EntityIdHelper.validateId(_id, root);
        EntityIdHelper.validateId(id, event);
        
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setVersion(event, version);
        event.setTenantId(getTenantId());
        event.setId(root.id());
        event.setRootId(root.id());
        event.setAction(Event.Action.UPDATE);
        root.applyEvent(event);
        EntityHelper.setUpdatedDate(root, event.getMeta().createdDate());
        validator.accept(root);
        addEvent(event);
        getCrudOperations().update(root);
        getEventPublisher().accept(event);
        return root;
    }

    @Override
    public T delete(String id) {
        Objects.requireNonNull(id);
        if (_id == null) {
            throw new DomainEventException(DomainEventException.ErrorCode.BASIC_VIOLATION,"root tid not available for entity {0}", entityType.getSimpleName());
        }

        T root = (T) this.<T>getEntityProvider().loadEntity(entityType, id, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity id {0} not available for entity {1}", id, entityType.getSimpleName()));

        EntityIdHelper.validateId(id, root);
        EntityIdHelper.validateId(_id, root);
        
        EntityDeleted event = new EntityDeleted(entityType, entityType, root.entityId(), root.entityId());
        event.setId(root.id());
        event.setRootId(root.id());
        event.setTenantId(getTenantId());
        event.setAction(Event.Action.DELETE);
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setVersion(event, version);
        validator.accept(event);
        addEvent(event);
        getCrudOperations().delete(root);
        getEventPublisher().accept(event);
        return root;
    }

    @Override
    public T rename(String id, String newId) {
        EntityIdHelper.validateEntityId(id);
        EntityIdHelper.validateEntityId(newId);

        if (_id == null) {
            throw new DomainEventException(DomainEventException.ErrorCode.BASIC_VIOLATION,"root tid not available for entity {0}", entityType.getSimpleName());
        }

        T root = (T) this.<T>getEntityProvider().loadEntity(entityType, id, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity id {0} not available for entity {1}", id, entityType.getSimpleName()));

        EntityIdHelper.validateId(id, root);
        EntityIdHelper.validateId(_id, root);
        
        EntityRenamed event = new EntityRenamed(entityType, entityType, newId, id, newId);
        event.setTenantId(getTenantId());
        event.setId(root.id());
        event.setRootId(root.id());
        event.setAction(Event.Action.RENAME);
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setVersion(event, version);
        validator.accept(event);
        addEvent(event);
        T old = root;
        root = root.rename(newId);
        validator.accept(root);
        EntityHelper.setUpdatedDate(root, event.getMeta().createdDate());
        getCrudOperations().rename(old, root);
        getEventPublisher().accept(event);
        return root;
    }

    @Override
    public ChildEntityContext asChildContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<T> getEntityById(String id) {
        return this.<T>getQueryOperations().getRootById(entityType, id, getTenantId());
    }

    @Override
    public <C extends ChildEntity<T>> Optional<C> getChildEntityById(Class<C> childType, String id) {
        EntityIdHelper.validateTechnicalId(_id);
        return this.<T>getQueryOperations().getChildById(entityType, _id, childType, id, getTenantId());
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<C> getAllChildEntitiesByType(Class<C> childType,Query.PagingRequest pageable) {
        return this.<T>getQueryOperations().getAllChildByType(entityType, _id, childType, getTenantId(),pageable);
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
    public Optional<T> getEntity() {
        if (this._id == null) {
            return Optional.empty();
        }
        return getEntityById(_id);
    }

    @Override
    public ResultSet<T> getAll(Query.PagingRequest pagingRequest) {
        return this.<T>getQueryOperations().getAllByRootType(entityType, getTenantId(), pagingRequest);
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<Event<C>> getChildEntityEventsById(Class<C> childType, String id,Query.PagingRequest pageRequest) {
        return this.<T>getQueryOperations().getEventsByChildId(entityType, _id, childType, id,getTenantId(), pageRequest);
    }

    @Override
    public  ResultSet<Event<T>> getEntityEventsById(String id,Query.PagingRequest pageRequest) {
        return  this.<T>getQueryOperations().getEventsByRootId(entityType, _id, getTenantId(), pageRequest);
    }

    @Override
    public AsyncEntityContext<T> asAsyncEntityContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AsyncRootEntityQueryContext<T> asAsyncQueryContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private RootEntityContext<T>  init(String id)
    {
        _id = getEntityById(id).get().id();
        return this;
    }
    
    protected String getId()
    {
        return _id;
    }
    public RootEntityContext<T> asNonTenantContext(String id){
        return new RootEntityContext<>(entityType,null, null, entitySupplier, idGenerator, crudOperations, tx, eventPublisher, validator, queryOperationSelector, version).init(id);
    }
}
