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
public class RootEntityContext<T extends RootEntity> extends EntityContext<T> implements RootEntityQueryContext<T> {

    private String _id;

    public RootEntityContext(Class<T> entityType, String tid, String tenantId, EntityProvider<T> entitySupplier,
            Supplier<String> idGenerator, CRUDOperations crudOperations, QueryOperations<T> queryOperation,
            Consumer<Event> eventPublisher,Consumer<Object> validator,Function<Class<? extends RootEntity> ,QueryOperations<?>> queryOperationSelector) {
        super(entityType, tenantId, entitySupplier, idGenerator, crudOperations, queryOperation, eventPublisher,validator,queryOperationSelector);
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
        crudOperations.create(root);
        eventPublisher.accept(event);
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
        event.setTenantId(getTenantId());
        event.setId(_id);
        event.setRootId(_id);
        event.setAction(Event.Action.UPDATE);
        root.applyEvent(event);
        EntityHelper.setUpdatedDate(root, event.getMeta().createdDate());
        validator.accept(root);
        addEvent(event);
        crudOperations.update(root);
        eventPublisher.accept(event);
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
        event.setId(_id);
        event.setRootId(_id);
        event.setTenantId(getTenantId());
        event.setAction(Event.Action.DELETE);
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        validator.accept(event);
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
            throw new DomainEventException(DomainEventException.ErrorCode.BASIC_VIOLATION,"root tid not available for entity {0}", entityType.getSimpleName());
        }

        T root = (T) this.<T>getEntityProvider().loadEntity(entityType, id, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity id {0} not available for entity {1}", id, entityType.getSimpleName()));

        EntityIdHelper.validateId(id, root);
        EntityIdHelper.validateId(_id, root);
        
        EntityRenamed event = new EntityRenamed(entityType, entityType, newId, id, newId);
        event.setTenantId(getTenantId());
        event.setId(_id);
        event.setRootId(_id);
        event.setAction(Event.Action.RENAME);
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        validator.accept(event);
        addEvent(event);
        T old = root;
        root = root.rename(newId);
        validator.accept(root);
        EntityHelper.setUpdatedDate(root, event.getMeta().createdDate());
        crudOperations.rename(old, root);
        eventPublisher.accept(event);
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
    public <C extends ChildEntity<T>> Collection<C> getAllChildEntitiesByType(Class<C> childType) {
        return this.<T>getQueryOperations().getAllChildByType(entityType, _id, childType, getTenantId());
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
}
