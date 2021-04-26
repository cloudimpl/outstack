/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.Command;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwansa
 * @param <R>
 * @param <T>
 * @param <I>
 */
public abstract class EntityCommandHandler<T extends Entity,I extends Command,R> implements CommandHandler<EntityContext<T>,I, R>
{
    private final Class<T> enityType;

    public EntityCommandHandler() {
        this.enityType = Util.extractGenericParameter(this.getClass(), EntityCommandHandler.class, 0);
    }
    
    public boolean isTenantFunction()
    {
        return Entity.hasTenant(enityType);
    }
    
    @Override
    public R apply(EntityContext<T> context,I command)
    {
        validateInput(command);
        return execute(context, command);
    }
    
    protected abstract R execute(EntityContext<T> context,I command);
    
    private void validateInput(I command)
    {
        if(isTenantFunction() && command.tenantId() == null)
        {
            throw new CommandException("tenantId is not available in the request");
        }
    }
    
    public EntityContext<T>  emit(EntityContextProvider.Transaction tx,I command)
    {
        EntityContext<T> context = tx.getContext(enityType);
        R reply = apply(context, command);
        tx.setReply(reply);
        return context;
    }
}