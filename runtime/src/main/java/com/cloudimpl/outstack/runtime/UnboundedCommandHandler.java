package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.*;
import reactor.core.publisher.Mono;

public abstract class UnboundedCommandHandler<T extends RootEntity,C extends Command, R> extends EntityCommandHandler<T, C, Mono<R>>{


    public Mono<EntityContext<T>>  emitAsync(EntityContextProvider contextProvider, ICommand input)
    {
        if(input.version() != null && !contextProvider.getVersion().equals(input.version()))
        {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_VERSION,"invalid version {0} ,expecting {1}", input.version(),contextProvider.getVersion());
        }
        C cmd = input.unwrap(this.cmdType);
        validateInput(cmd);
        EntityContextProvider.UnboundedTransaction tx = contextProvider.createUnboundedTransaction(getTenantRequirement() == TenantRequirement.NONE? null:cmd.tenantId());
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
        UnboundedEntityContext<T> context = (UnboundedEntityContext<T>) tx.getContext(entityType);
        context.setTx(tx);
        return apply(context, (C) cmd).doOnNext(r -> tx.setReply(r)).map(r -> context);
    }

    public Mono<R> apply(EntityContext<T> context, C command) {
        validateInput(command);
        return execute(context, command);
    }

    protected abstract Mono<R> execute(EntityContext<T> context, C command);


}
