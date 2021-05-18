/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 * @param <T>
 * @param <C>
 */
public abstract class AsyncEntityCommandHandler<T extends Entity,C extends  Command> extends EntityCommandHandler<T, C, Mono>{

    
    @Override
    public EntityContext<T>  emit(EntityContextProvider contextProvider,ICommand input)
    {
        if(!contextProvider.getVersion().equals(input.version()))
        {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_VERSION,"invalid version {0} ,expecting {1}", input.version(),contextProvider.getVersion());
        }
        C cmd = input.unwrap(this.cmdType);
        validateInput(cmd);
        EntityContextProvider.Transaction tx = contextProvider.createWritableTransaction(cmd.rootId(), cmd.tenantId(),true);
        EntityContext<T> context = tx.getContext(enityType);
        context.setTx(tx);
        Mono reply = apply(context, (C) cmd);
        tx.setReply(reply);
        return context;
    }
}
