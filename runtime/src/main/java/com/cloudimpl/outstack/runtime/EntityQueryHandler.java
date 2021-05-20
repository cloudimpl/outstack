/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
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
public abstract class EntityQueryHandler<T extends Entity, I extends Query, R> implements Handler<T> {

    protected final Class<T> entityType;
    protected final Class<I> queryType;

    public EntityQueryHandler() {
        this.entityType = Util.extractGenericParameter(this.getClass(), EntityQueryHandler.class, 0);
        this.queryType = Util.extractGenericParameter(this.getClass(), EntityQueryHandler.class, 1);
    }

    public EntityQueryHandler(Class<T> entityType) {
        this.entityType = entityType;
        this.queryType = Util.extractGenericParameter(this.getClass(), EntityQueryHandler.class, 1);
    }

    public boolean isTenantFunction() {
        return Entity.hasTenant(entityType);
    }

    public R apply(EntityQueryContext<T> context, I query) {
        validateInput(query);
        return execute(context, query);
    }

    protected abstract R execute(EntityQueryContext<T> context, I query);

    protected void validateInput(I query) {
        if (isTenantFunction() && query.tenantId() == null) {
            throw new QueryException("tenantId is not available in the request");
        }
    }

    public R emit(EntityQueryContextProvider contextProvider, IQuery input) {
        if (!contextProvider.getVersion().equals(input.version())) {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_VERSION, "invalid version {0} ,expecting {1}", input.version(), contextProvider.getVersion());
        }
        I query = input.unwrap(this.queryType);
        validateInput(query);
        EntityQueryContextProvider.ReadOnlyTransaction tx = contextProvider.createTransaction(query.rootId(), query.tenantId(),false);
        EntityQueryContext<T> context = tx.getContext(this.entityType);
        //context.setTx(tx);
        return apply(context, query);
    }
}
