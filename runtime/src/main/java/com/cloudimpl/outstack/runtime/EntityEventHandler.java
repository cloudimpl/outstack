/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwan
 * @param <T>
 * @param <E>
 */
public abstract class EntityEventHandler<T extends Entity, E extends Event<?>> implements CommandHandler<T>{

    private final Class<T> enityType;
    private final Class<E> eventType;
    public EntityEventHandler() {
        this.enityType = Util.extractGenericParameter(this.getClass(), EntityEventHandler.class, 0);
        this.eventType = Util.extractGenericParameter(this.getClass(), EntityEventHandler.class, 1);
    }

    public boolean isTenantFunction() {
        return Entity.hasTenant(enityType);
    }

    public void accept(EntityContext<T> context, E event) {
        validateInput(event);
        execute(context, event);
    }

    protected abstract void execute(EntityContext<T> context, E event);
    
    private void validateInput(Event<?> event)
    {
        if(isTenantFunction() && event.tenantId() == null)
        {
            throw new CommandException("tenantId is not available in the request");
        }
    }
    
    protected EntityContext<T> emit(EntityContextProvider.Transaction tx,E event)
    {
        EntityContext<T> context = tx.getContext(enityType);
        accept(context, event);
        return context;
    }
}
