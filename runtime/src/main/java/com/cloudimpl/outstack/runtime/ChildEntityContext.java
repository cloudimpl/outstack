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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author nuwan
 * @param <R>
 * @param <T>
 */
public class ChildEntityContext<R extends RootEntity, T extends ChildEntity<R>> extends EntityContext<T> {

    private final Class<R> rootType;
    private final String rootTid;

    public ChildEntityContext(Class<R> rootType, String rootTid, Class<T> entityType, String tenantId, Function<String, ? extends Entity> entitySupplier, Supplier<String> idGenerator, ResourceHelper resourceHelper, CRUDOpertations crudOperations, Consumer<Event> eventPublisher) {
        super(entityType, tenantId, entitySupplier, idGenerator, resourceHelper, crudOperations, eventPublisher);
        this.rootType = rootType;
        this.rootTid = rootTid;
    }

    @Override
    public T create(String id, Event<T> event) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootTid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", event.getRootEntityRN());
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId())));
        if (child != null) {
            throw new DomainEventException("child entity {0} is already exist", child.getRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        child = root.createChildEntity(entityType, id, idGenerator.get());
        event.setTenantId(getTenantId());
        event.setRootTid(root.tid());
        event.setTid(child.tid());
        event.setAction(Event.Action.CREATE);
        child.applyEvent(event);
        addEvent(event);
        crudOperations.create(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T update(String id, Event<T> event) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootTid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", event.getRootEntityRN());
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId())));
        if (child == null) {
            throw new DomainEventException("child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId()));
        }
        if (!child.id().equals(id)) {
            throw new DomainEventException("invalid id {0} for child entity {1}", id, child.getRN());
        }
        if (!event.entityId().equals(id)) {
            throw new DomainEventException("event id and given id not equal. {0} , {1}", id, event.entityId());
        }
        child = root.createChildEntity(entityType, id, idGenerator.get());
        event.setTenantId(getTenantId());
        event.setRootTid(root.tid());
        event.setTid(child.tid());
        event.setAction(Event.Action.UPDATE);
        child.applyEvent(event);
        addEvent(event);
        crudOperations.update(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T delete(String id) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootTid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", RootEntity.makeTRN(rootType, rootTid, getTenantId()));
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId())));
        if (child == null) {
            throw new DomainEventException("child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.id(), entityType, id, getTenantId()));
        }
        if (!root.id().equals(id)) {
            throw new DomainEventException("invalid id {0} for child entity {1}", id, child.getRN());
        }
        EntityDeleted event = new EntityDeleted(entityType, rootType, child.id(), root.id());
        event.setTenantId(getTenantId());
        event.setRootTid(root.tid());
        event.setTid(child.tid());
        event.setAction(Event.Action.DELETE);
        addEvent(event);
        crudOperations.delete(child);
        eventPublisher.accept(event);
        return child;
    }

    @Override
    public T rename(String id, String newId) {
        R root = (R) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, rootTid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity {0} is not exist", RootEntity.makeTRN(rootType, rootTid, getTenantId()));
        }
        T child = (T) entitySupplier.apply(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, root.tid(), entityType, id, getTenantId())));
        if (child == null) {
            throw new DomainEventException("child entity {0} is does not exist", ChildEntity.makeRN(rootType, root.tid(), entityType, id, getTenantId()));
        }
        if (!root.id().equals(id)) {
            throw new DomainEventException("invalid id {0} for child entity {1}", id, child.getRN());
        }
        EntityRenamed event = new EntityRenamed(entityType, rootType, newId, id, root.id());
        event.setTenantId(getTenantId());
        event.setRootTid(root.tid());
        event.setTid(child.tid());
        event.setAction(Event.Action.RENAME);
        addEvent(event);
        child = child.rename(newId);
        crudOperations.rename(id, child);
        eventPublisher.accept(event);
        return child;
    }

}
