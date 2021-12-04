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

import java.lang.ref.Reference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class ExternalTrigger implements StatefullWork {

    private CompletableFuture<WorkStatus> future = new CompletableFuture();
    private WorkContext context;

    protected ExternalTrigger() {

    }

    @Override
    public Mono<WorkStatus> execute(WorkContext context) {
        this.context = context;
        return Mono.fromFuture(future);
    }

    public synchronized <T, U> Mono<T> trigger(Function<WorkContext, U> handler) {
        if (this.context == null) {
            return Mono.error(new WorkflowException("External trigger {0} not active yet", this.getClass().getName()));
        }
        Object out = handler.apply(context);
        AtomicReference<Status> reference = new AtomicReference<>(Status.COMPLETED);
        if (out instanceof Mono) {
            return ((Mono) out).map(o -> mapOutCome(o, reference)).doOnSuccess(o -> future.complete(WorkStatus.publish(reference.get(), context)));
        } else {
            return Mono.just(out).map(o -> (T) mapOutCome(o, reference)).doOnSuccess(o -> future.complete(WorkStatus.publish(reference.get(), context)));
        }
    }

    private <T> T mapOutCome(Object obj, AtomicReference<Status> reference) {
        if (obj instanceof WorkStatus) {
            WorkStatus st = WorkStatus.class.cast(obj);
            reference.set(st.getStatus());
            return st.getData();
        } else {
            return (T) obj;
        }
    }
}
