/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.util.Util;

/**
 * @param <R>
 * @param <T>
 * @param <I>
 * @author nuwansa
 */
public abstract class EntityCommandHandler<T extends Entity, I extends Command, R> implements CommandHandler<T> {
    private final Class<T> entityType;
    private final Class<I> cmdType;

    public EntityCommandHandler() {
        this.entityType = Util.extractGenericParameter(this.getClass(), EntityCommandHandler.class, 0);
        this.cmdType = Util.extractGenericParameter(this.getClass(), EntityCommandHandler.class, 1);
    }

    public EntityCommandHandler(Class<T> type) {
        this.entityType = type;
        this.cmdType = Util.extractGenericParameter(this.getClass(), EntityCommandHandler.class, 1);
    }

    public boolean isTenantFunction() {
        return Entity.hasTenant(entityType);
    }

    public R apply(EntityContext<T> context, I command) {
        validateInput(command);
        return execute(context, command);
    }

    protected abstract R execute(EntityContext<T> context, I command);

    private void validateInput(I command) {
        if (isTenantFunction() && command.tenantId() == null) {
            throw new CommandException("tenantId is not available in the request");
        }
    }

    public EntityContext<T> emit(EntityContextProvider contextProvider, ICommand input) {
        I cmd = input.unwrap(this.cmdType);
        validateInput(cmd);
        EntityContextProvider.Transaction tx = contextProvider.createWritableTransaction(cmd.rootId(), cmd.tenantId());
        EntityContext<T> context = tx.getContext(entityType);
        context.setTx(tx);
        R reply = apply(context, (I) cmd);
        tx.setReply(reply);
        return context;
    }

    public Class<T> getEntityType() {
        return entityType;
    }
}