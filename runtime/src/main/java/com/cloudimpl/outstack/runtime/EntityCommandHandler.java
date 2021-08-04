/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.domainspec.TenantRequirement;
import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwansa
 * @param <R>
 * @param <T>
 * @param <I>
 */
public abstract class EntityCommandHandler<T extends Entity, I extends Command, R> implements CommandHandler<T> {

    public static final CommandResponse OK = new CommandResponse("OK");

    protected final Class<T> entityType;
    protected final Class<I> cmdType;

    public EntityCommandHandler() {
        this.entityType = Util.extractGenericParameter(this.getClass(), EntityCommandHandler.class, 0);
        this.cmdType = Util.extractGenericParameter(this.getClass(), EntityCommandHandler.class, 1);
    }

    public EntityCommandHandler(Class<T> type) {
        this.entityType = type;
        this.cmdType = Util.extractGenericParameter(this.getClass(), EntityCommandHandler.class, 1);
    }

    public TenantRequirement getTenantRequirement() {
        return Entity.checkTenantRequirement(entityType);
    }

    public R apply(EntityContext<T> context, I command) {
        validateInput(command);
        return execute(context, command);
    }

    protected abstract R execute(EntityContext<T> context, I command);

    protected void validateInput(I command) {
        if (getTenantRequirement() == TenantRequirement.REQUIRED && command.tenantId() == null) {
            throw new CommandException("tenantId is not available in the request");
        }
    }

    protected EntityContext<T> emit(EntityContextProvider contextProvider, ICommand input) {
        if (!contextProvider.getVersion().equals(input.version())) {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_VERSION, "invalid version {0} ,expecting {1}", input.version(), contextProvider.getVersion());
        }
        I cmd = input.unwrap(this.cmdType);
        validateInput(cmd);
        EntityContextProvider.Transaction tx = contextProvider.createWritableTransaction(cmd.rootId(), getTenantRequirement() == TenantRequirement.NONE ? null : cmd.tenantId(), false);
        tx.setInputMetaProvider(new InputMetaProvider() {
            @Override
            public String getUserName() {
                return cmd.getMapAttr().get("userName");
            }

            @Override
            public String getUserId() {
                return cmd.getMapAttr().get("userId");
            }
        });
        EntityContext<T> context = (EntityContext<T>) tx.getContext(entityType);
        context.setTx(tx);
        R reply = apply(context, (I) cmd);
        tx.setReply(reply);
        return context;
    }

    public static CommandResponse OK() {
        return OK;
    }
}
