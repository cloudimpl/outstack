/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.domainspec.IQuery;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwan
 * @param <T>
 * @param <I>
 * @param <R>
 */
public abstract class EntityQueryHandler<T extends Entity,I extends Query,R> implements Handler<T>{
    private final Class<T> entityType;
    private final Class<I> queryType;

    public EntityQueryHandler() {
        this.entityType = Util.extractGenericParameter(this.getClass(), EntityQueryHandler.class, 0);
        this.queryType = Util.extractGenericParameter(this.getClass(), EntityQueryHandler.class, 1);
    }
    
    public boolean isTenantFunction()
    {
        return Entity.hasTenant(entityType);
    }
    
    public  R apply(EntityQueryContext<T> context,I query)
    {
        validateInput(query);
        return execute(context, query);
    }
    
    protected abstract  R execute(EntityQueryContext<T> context,I query);
    
    private void validateInput(I query)
    {
        if(isTenantFunction() && query.tenantId() == null)
        {
            throw new QueryException("tenantId is not available in the request");
        }
    }
    
     public EntityContext<T>  emit(EntityContextProvider contextProvider,IQuery input)
    {
        I query = input.unwrap(this.queryType);
        validateInput(query);
        EntityContextProvider.Transaction tx = contextProvider.createTransaction(query.rootId(), query.tenantId());
        EntityContext<T> context = tx.getContext(this.entityType);
        context.setTx(tx);
        R reply = apply((EntityQueryContext<T>) context, query);
        tx.setReply(reply);
        return context;
    }
}
