/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
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
public class ChildEntityContext<R extends RootEntity, T extends ChildEntity<R>> extends EntityContext<T> implements ChildEntityQueryContext<R, T> {

    private final Class<R> rootType;
    private final String rootId;

    public ChildEntityContext(Class<R> rootType, String rootId, Class<T> entityType, String tenantId, EntityProvider<R> entitySupplier,
            Supplier<String> idGenerator, CRUDOperations crudOperations,
            QueryOperations<R> queryOperation, Consumer<Event> eventPublisher,Consumer<Object> validator,Function<Class<? extends RootEntity> ,QueryOperations<?>> queryOperationSelector) {
        super(entityType, tenantId, entitySupplier, idGenerator, crudOperations, queryOperation, eventPublisher,validator,queryOperationSelector);
        this.rootType = rootType;
        this.rootId = rootId;
        Objects.requireNonNull(this.rootId);
    }

    @Override
    public T create(String id, Event<T> event) {
        EntityIdHelper.validateEntityId(id);
        EntityIdHelper.validateTechnicalId(rootId);
        validator.accept(event); //validate event
        R root = (R) this.<R>getEntityProvider().loadEntity(rootType, rootId, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0} is not exist", event.getRootEntityRN()));

        this.<R>getEntityProvider().loadEntity(rootType, root.id(), entityType, id, getTenantId()).ifPresent(e -> {
            throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_EXIST,"child entity {0} is already exist", e.getBRN());
        });
        if (!event.entityId().equals(id)) {
            throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_EVENT_RELATION_VIOLATION,"event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        if (!root.entityId().equals(event.rootEntityId())) {
            throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_EVENT_RELATION_VIOLATION,"root entity Id and event root id not equal. {0} , {1}", root.entityId(), event.rootEntityId());
        }

        T child = root.createChildEntity(entityType, id, idGenerator.get());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.CREATE);
        child.applyEvent(event);
        addEvent(event);
        validator.accept(child);
        crudOperations.create(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T update(String id, Event<T> event) {
        EntityIdHelper.validateTechnicalId(rootId);
        validator.accept(event); //validate event
        R root = (R) this.<R>getEntityProvider().loadEntity(rootType, rootId, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0} is not exist", event.getRootEntityRN()));

        T child = (T) this.<R>getEntityProvider().loadEntity(rootType, root.id(), entityType, id, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.entityId(), entityType, id, getTenantId())));

        if (!event.rootEntityId().equals(root.entityId())) {
            throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_EVENT_RELATION_VIOLATION,"invalid root entity id {0} in the event faor root entity {1}", event.rootEntityId(), root.entityId());
        }
        
        EntityIdHelper.validateId(id, child);
        EntityIdHelper.validateId(id, event);
       
        child = root.createChildEntity(entityType, id, idGenerator.get());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.UPDATE);
        child.applyEvent(event);
        validator.accept(child);
        addEvent(event);
        crudOperations.update(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T delete(String id) {
        EntityIdHelper.validateTechnicalId(rootId);
        R root = (R) this.<R>getEntityProvider().loadEntity(rootType, rootId, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0}:{1} is not exist", rootType.getSimpleName(), rootId));

        T child = (T) this.<R>getEntityProvider().loadEntity(rootType, root.id(), entityType, id, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.entityId(), entityType, id, getTenantId())));
        
        EntityIdHelper.validateId(id, child);

        EntityDeleted event = new EntityDeleted(entityType, rootType, child.entityId(), root.entityId());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.DELETE);
        validator.accept(event); //validate event
        addEvent(event);
        crudOperations.delete(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T rename(String id, String newId) {
        EntityIdHelper.validateTechnicalId(rootId);
        R root = (R) this.<R>getEntityProvider().loadEntity(rootType, rootId, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0}:{1} is not exist", rootType.getSimpleName(), rootId));

        T child = (T) this.<R>getEntityProvider().loadEntity(rootType, root.id(), entityType, id, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.entityId(), entityType, id, getTenantId())));
        
        EntityIdHelper.validateId(id, child);
  
        EntityRenamed event = new EntityRenamed(entityType, rootType, newId, id, root.entityId());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.RENAME);
        validator.accept(event); //validate event
        addEvent(event);
        T old = child;
        child = child.rename(newId);
        validator.accept(child); 
        crudOperations.rename(old, child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public <R extends RootEntity> RootEntityContext<R> asRootContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChildEntityContext<R, T> asChildContext() {
        return this;
    }

    @Override
    public Optional<T> getEntityById(String id) {
        EntityIdHelper.validateTechnicalId(rootId);
        return this.<R>getQueryOperations().getChildById(rootType, rootId, entityType, id, getTenantId());
    }

    @Override
    public R getRoot() {
        return (R) this.<R>getQueryOperations().getRootById(rootType, rootId, getTenantId()).orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0} not found for id {1}", rootType, rootId));
    }

    @Override
    public Collection<T> getAllByEntityType(Class<T> type) {
        return this.<R>getQueryOperations().getAllChildByType(rootType, rootId, type, getTenantId());
    }

    @Override
    public <R extends RootEntity> RootEntityQueryContext<R> asRootQueryContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChildEntityQueryContext<R, T> asChildQueryContext() {
        return this;
    }
}
