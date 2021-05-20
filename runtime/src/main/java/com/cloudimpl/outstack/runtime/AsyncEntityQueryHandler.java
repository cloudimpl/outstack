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

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.IQuery;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import org.reactivestreams.Publisher;

/**
 *
 * @author nuwan
 * @param <T>
 * @param <I>
 */
public abstract class AsyncEntityQueryHandler<T extends RootEntity, I extends Query, R>  extends EntityQueryHandler<T, I, Publisher<R>>{

    public Publisher<R> emitAsync(EntityQueryContextProvider contextProvider, IQuery input) {
        if (!contextProvider.getVersion().equals(input.version())) {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_VERSION, "invalid version {0} ,expecting {1}", input.version(), contextProvider.getVersion());
        }
        I query = input.unwrap(this.queryType);
        validateInput(query);
        EntityQueryContextProvider.ReadOnlyTransaction tx = contextProvider.createTransaction(query.rootId(), query.tenantId(),true);
        EntityQueryContext<T> context = tx.getContext(this.entityType);
        //context.setTx(tx);
        return apply(context, query);
    }
}
