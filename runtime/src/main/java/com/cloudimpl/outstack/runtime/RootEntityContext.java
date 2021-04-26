/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.DomainEventException;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.EntityDeleted;
import com.cloudimpl.outstack.runtime.domain.v1.EntityRenamed;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;
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

    public RootEntityContext(Class<T> entityType, String tenantId, Function<String, ? extends Entity> entitySupplier, Supplier<String> idGenerator, ResourceHelper resourceHelper,CRUDOpertations crudOperations,Consumer<Event> eventPublisher) {
        super(entityType, tenantId, entitySupplier, idGenerator, resourceHelper,crudOperations,eventPublisher);
    }

    protected void setTid(String tid) {
        this.tid = tid;
    }

    @Override
    public T create(String id, Event<T> event) {
        T root = (T) entitySupplier.apply(resourceHelper.getFQBrn(RootEntity.makeRN(entityType, id, getTenantId())));
        if (root != null) {
            throw new DomainEventException("root entity {0} already exist", root.getRN());
        }
        root = RootEntity.create(entityType, id, getTenantId(), idGenerator.get());
        
        event.setTenantId(getTenantId());
        event.setTid(root.tid());
        event.setRootTid(root.tid());
        event.setAction(Event.Action.CREATE);
        root.applyEvent(event);
        addEvent(event);
        setTid(root.tid());
        crudOperations.create(root);
        eventPublisher.accept(event);
        return root;
    }

    @Override
    public T update(String id, Event<T> event) {
        if (tid == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }
        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this.tid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity not available for entity {0}", entityType.getSimpleName());
        }
        if(!root.id().equals(id))
        {
            throw new DomainEventException("update failed,invalid id {0} for root entity {1}", id,root.getRN());
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
        if (tid == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }
        
        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this.tid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity id {0} not available for entity {1}",id, entityType.getSimpleName());
        }
        if(!root.id().equals(id))
        {
            throw new DomainEventException("invalid id {0} for root entity {1}", id,root.getRN());
        }
        EntityDeleted event = new EntityDeleted(entityType,entityType,root.id(),root.id());
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
         if (tid == null) {
            throw new DomainEventException("root tid not available for entity {0}", entityType.getSimpleName());
        }
        
        T root = (T) entitySupplier.apply(resourceHelper.getFQTrn(RootEntity.makeTRN(entityType, this.tid, getTenantId())));
        if (root == null) {
            throw new DomainEventException("root entity id {0} not available for entity {1}",id, entityType.getSimpleName());
        }
        if(!root.id().equals(id))
        {
            throw new DomainEventException("invalid id {0} for root entity {1}", id,root.getRN());
        }
        EntityRenamed event = new EntityRenamed(entityType, entityType, newId, id,newId);
        event.setTenantId(getTenantId());
        event.setTid(tid);
        event.setRootTid(tid);
        event.setAction(Event.Action.RENAME);
        addEvent(event);
        root = root.rename(newId);
        crudOperations.rename(id,root);
        eventPublisher.accept(event);
        return root;
    }

}
