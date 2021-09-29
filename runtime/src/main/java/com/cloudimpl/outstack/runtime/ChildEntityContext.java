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
 * @param <R>
 * @param <T>
 */
public class ChildEntityContext<R extends RootEntity, T extends ChildEntity<R>> extends EntityContext<T> implements ChildEntityQueryContext<R, T> {

    private final Class<R> rootType;
    private final String rootId;

    public ChildEntityContext(Class<R> rootType, String rootId, Class<T> entityType, String tenantId, Optional<EntityProvider<? extends RootEntity>> entitySupplier,
            Supplier<String> idGenerator, Optional<CRUDOperations> crudOperations,
            QueryOperations<R> queryOperation, Optional<Consumer<Event>> eventPublisher,Consumer<Object> validator,
            Function<Class<? extends RootEntity> ,QueryOperations<?>> queryOperationSelector,String version) {
        super(entityType, tenantId, entitySupplier, idGenerator, crudOperations, queryOperation, eventPublisher,validator,queryOperationSelector,version);
        this.rootType = rootType;
        this.rootId = rootId;
        Objects.requireNonNull(this.rootId);
    }

    @Override
    public T create(String id, Event<T> event) {
        EntityIdHelper.validateEntityId(id);
        EntityIdHelper.validateTechnicalId(rootId);
        EntityHelper.validateEvent(rootType,event);
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
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setUserId(event, getTx().getInputMetaProvider().getUserId());
        EntityHelper.setUserName(event, getTx().getInputMetaProvider().getUserName());
        EntityHelper.setVersion(event, version);
        T child = root.createChildEntity(entityType, id, idGenerator.get());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.CREATE);
    
        EntityHelper.applyEvent(child,event);
        EntityHelper.setCreatedDate(child, event.getMeta().createdDate());
        EntityHelper.setUpdatedDate(child, event.getMeta().createdDate());
        EntityHelper.setUserId(child, getTx().getInputMetaProvider().getUserId());
        EntityHelper.setUserName(child, getTx().getInputMetaProvider().getUserName());
        addEvent(event);
        validator.accept(child);
        getCrudOperations().create(child);
        getEventPublisher().accept(event);
        return child;
    }

    @Override
    public T update(String id, Event<T> event) {
        EntityIdHelper.validateTechnicalId(rootId);
        validator.accept(event); //validate event
        EntityHelper.validateEvent(rootType,event);
        R root = (R) this.<R>getEntityProvider().loadEntity(rootType, rootId, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0} is not exist", event.getRootEntityRN()));

        T child = (T) this.<R>getEntityProvider().loadEntity(rootType, root.id(), entityType, id, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"child entity {0} is does not exist", ChildEntity.makeRN(rootType,getVersion(), root.entityId(), entityType, id, getTenantId())));

        if (!event.rootEntityId().equals(root.entityId())) {
            throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_EVENT_RELATION_VIOLATION,"invalid root entity id {0} in the event faor root entity {1}", event.rootEntityId(), root.entityId());
        }
        
        EntityIdHelper.validateId(id, child);
        EntityIdHelper.validateId(id, event);
       
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setUserId(event, getTx().getInputMetaProvider().getUserId());
        EntityHelper.setUserName(event, getTx().getInputMetaProvider().getUserName());
        EntityHelper.setVersion(event, version);
        
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        event.setAction(Event.Action.UPDATE);
        EntityHelper.applyEvent(child,event);
        EntityHelper.setUpdatedDate(child, event.getMeta().createdDate());
        EntityHelper.setUserId(child, getTx().getInputMetaProvider().getUserId());
        EntityHelper.setUserName(child, getTx().getInputMetaProvider().getUserName());
        validator.accept(child);
        addEvent(event);
        getCrudOperations().update(child);
        getEventPublisher().accept(event);
        return child;
    }

    @Override
    public T delete(String id) {
        EntityIdHelper.validateTechnicalId(rootId);
        R root = (R) this.<R>getEntityProvider().loadEntity(rootType, rootId, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0}:{1} is not exist", rootType.getSimpleName(), rootId));

        T child = (T) this.<R>getEntityProvider().loadEntity(rootType, root.id(), entityType, id, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"child entity {0} is does not exist", ChildEntity.makeRN(rootType,getVersion(), root.entityId(), entityType, id, getTenantId())));
        
        EntityIdHelper.validateId(id, child);

        EntityDeleted event = new EntityDeleted(entityType, rootType, child.entityId(), root.entityId());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setUserId(event, getTx().getInputMetaProvider().getUserId());
        EntityHelper.setUserName(event, getTx().getInputMetaProvider().getUserName());
        EntityHelper.setVersion(event, version);
        event.setAction(Event.Action.DELETE);
        validator.accept(event); //validate event
        addEvent(event);
        getCrudOperations().delete(child);
        getEventPublisher().accept(event);
        return child;
    }

    @Override
    public T rename(String id, String newId) {
        EntityIdHelper.validateTechnicalId(rootId);
        R root = (R) this.<R>getEntityProvider().loadEntity(rootType, rootId, null, null, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"root entity {0}:{1} is not exist", rootType.getSimpleName(), rootId));

        T child = (T) this.<R>getEntityProvider().loadEntity(rootType, root.id(), entityType, id, getTenantId())
                .orElseThrow(() -> new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"child entity {0} is does not exist", ChildEntity.makeRN(rootType,getVersion(), root.entityId(), entityType, id, getTenantId())));
        
        EntityIdHelper.validateId(id, child);
  
        EntityRenamed event = new EntityRenamed(entityType, rootType, newId, id, root.entityId());
        event.setTenantId(getTenantId());
        event.setRootId(root.id());
        event.setId(child.id());
        EntityHelper.setCreatedDate(event, System.currentTimeMillis());
        EntityHelper.setUserId(event, getTx().getInputMetaProvider().getUserId());
        EntityHelper.setUserName(event, getTx().getInputMetaProvider().getUserName());
        EntityHelper.setVersion(event, version);
        event.setAction(Event.Action.RENAME);
        validator.accept(event); //validate event
        addEvent(event);
        T old = child;
        child = child.rename(newId);
        EntityHelper.setUpdatedDate(child,event.getMeta().createdDate());
        EntityHelper.setUserId(child, getTx().getInputMetaProvider().getUserId());
        EntityHelper.setUserName(child, getTx().getInputMetaProvider().getUserName());
        validator.accept(child); 
        getCrudOperations().rename(old, child);
        getEventPublisher().accept(event);
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
    public ResultSet<T> getAllByEntityType(Class<T> type,Query.PagingRequest pageReq) {
        return this.<R>getQueryOperations().getAllChildByType(rootType, rootId, type, getTenantId(),pageReq);
    }

    @Override
    public <R extends RootEntity> RootEntityQueryContext<R> asRootQueryContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChildEntityQueryContext<R, T> asChildQueryContext() {
        return this;
    }

    @Override
    public ResultSet<Event<T>> getEntityEventsById(String id, Query.PagingRequest pageRequest) {
         return  this.<R>getQueryOperations().getEventsByChildId(rootType, rootId, entityType, id, getTenantId(), pageRequest);
    }

    @Override
    public <K> K executeRawQuery(String rawQuery) {
        return this.getQueryOperations().executeRawQuery(rawQuery);
    }

    @Override
    public <R extends RootEntity> AsyncEntityContext<R> asAsyncEntityContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <R extends RootEntity> AsyncRootEntityQueryContext<R> asAsyncQueryContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isIdExist(String id, String tenantId) {
        return getQueryOperations().isIdExist(id, tenantId);
    }
    
}
