/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.collection.error.CollectionException;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import com.cloudimpl.outstack.runtime.repo.EventRepositoy;

/**
 *
 * @author nuwansa
 * @param <T>
 */
public class EntityContext<T extends Entity> {

    private final String tenantId;
    private final EventRepositoy repo;
    private final Class<T> entityType;

    public EntityContext(Class<T> entityType, String tenantId, EventRepositoy repo) {
        this.tenantId = tenantId;
        this.entityType = entityType;
        this.repo = repo;
    }

    public String getTenantId() {
        return tenantId;
    }

    public <E extends Event> T apply(String id, E event) {
        if (event.getOwner() != entityType) {
            throw CollectionException.INVALID_OWNER(err -> err.setEvent(event.getClass().getSimpleName()).setEntity(this.entityType.getSimpleName()).setOwner(event.getOwner().getSimpleName()));
        }
        event.setTenantId(tenantId);

        return null;
    }

    public T getEntity(String id) {
        return null;
    }

}
