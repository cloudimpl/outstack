/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityDeleted;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class RootEntityContext<T extends RootEntity> extends EntityContext<T> {

    private String tid;

    public RootEntityContext(Class<T> entityType,String tid, String tenantId, Function<String, ? extends Entity> entitySupplier, Supplier<String> idGenerator, ResourceHelper resourceHelper, CRUDOpertations crudOperations, Consumer<Event> eventPublisher) {
        super(entityType, tenantId, entitySupplier, idGenerator, resourceHelper, crudOperations, eventPublisher);
        this.tid = tid;
    }

    @Override
    public T create(String id, Event<T> event) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(event);
        T root = (T) entitySupplier.apply(resourceHelper.getFQBrn(RootEntity.makeRN(entityType, id, getTenantId())));
        if (root != null) {
            throw new DomainEventException("root entity {0} already exist", root.getRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        root = RootEntity.create(entityType, id, getTenantId(), idGenerator.get());

        event.setTenantId(getTenantId());
        event.setTid(root.tid());
        event.setRootTid(root.tid());
        event.setAction(Event.Action.CREATE);
        root.applyEvent(event);
        addEvent(event);
        this.tid = root.tid();
        crudOperations.create(root);
        eventPublisher.accept(event);
        return root;
    }

    @Override
    public T update(String id, Event<T> event) {
         Objects.requireNonNull(id);
        Objects.requireNonNull(event);
        if (tid == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }
        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this.tid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity not available for entity {0}", entityType.getSimpleName());
        }
        if (!root.id().equals(id)) {
            throw new DomainEventException("update failed,invalid id {0} for root entity {1}", id, root.getRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        event.setTenantId(getTenantId());
        event.setTid(tid);
        event.setRootTid(tid);
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
        if (tid == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }

        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this.tid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity id {0} not available for entity {1}", id, entityType.getSimpleName());
        }
        if (!root.id().equals(id)) {
            throw new DomainEventException("invalid id {0} for root entity {1}", id, root.getRN());
        }

        EntityDeleted event = new EntityDeleted(entityType, entityType, root.id(), root.id());
        event.setTid(tid);
        event.setRootTid(tid);
        event.setTenantId(getTenantId());
        event.setAction(Event.Action.DELETE);
        addEvent(event);
        crudOperations.delete(root);
        eventPublisher.accept(event);
        return root;
    }

    @Override
    public T rename(String id, String newId) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(newId);
        if (tid == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }

        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this.tid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity id {0} not available for entity {1}", id, entityType.getSimpleName());
        }
        if (!root.id().equals(id)) {
            throw new DomainEventException("invalid id {0} for root entity {1}", id, root.getRN());
        }
       
        EntityRenamed event = new EntityRenamed(entityType, entityType, newId, id, newId);
        event.setTenantId(getTenantId());
        event.setTid(tid);
        event.setRootTid(tid);
        event.setAction(Event.Action.RENAME);
        addEvent(event);
        root = root.rename(newId);
        crudOperations.rename(id, root);
        eventPublisher.accept(event);
        return root;
    }

}
