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
package com.cloudimpl.outstack.workflow;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public  class ExternalTrigger implements StatefullWork {

    private CompletableFuture<WorkResult> future = new CompletableFuture();
    private WorkContext context;
    
    protected ExternalTrigger()
    {
        
    }
    
    @Override
    public Mono<WorkResult> execute(WorkContext context) {
        this.context = context;
        return Mono.fromFuture(future);
    }

    public synchronized <T> Mono<T> trigger(Function<WorkContext, T> handler) {
        if (this.context == null) {
            return Mono.error(new WorkflowException("External trigger {0} not active yet", this.getClass().getName()));
        }
        T out = handler.apply(context);
        if (out instanceof Mono) {
            return ((Mono) out).doOnSuccess(o -> future.complete(new WorkResult(Status.COMPLETED, context)));
        } else {
            return Mono.just(out).doOnSuccess(o -> future.complete(new WorkResult(Status.COMPLETED, context)));
        }
    }

}
