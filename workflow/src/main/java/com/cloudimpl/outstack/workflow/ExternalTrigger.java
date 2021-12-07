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

import com.cloudimpl.outstack.runtime.Context;
import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Slf4j
public class ExternalTrigger implements StatefullWork {

    private CompletableFuture<WorkStatus> future = new CompletableFuture();
    private WorkContext context;
    protected transient WorkUnit workUnit;
    private Map<String, String> labels = new HashMap<>();
    private final boolean isGate;
    protected ExternalTrigger() {
        this.isGate = false;
    }
    
    protected ExternalTrigger(boolean isGate) {
        this.isGate = isGate;
    }

    protected void init(WorkUnit workUnit) {
        this.workUnit = workUnit;
    }

    protected void setFuture(CompletableFuture<WorkStatus> future)
    {
        this.future = future;
    }
    
    public ExternalTrigger putLabel(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    public boolean isGate()
    {
        return this.isGate;
    }

    protected WorkUnit getWorkUnit() {
        return workUnit;
    }
    
    
    @Override
    public Mono<WorkStatus> execute(WorkContext context) {
        this.context = context;
        this.labels.entrySet().stream().forEach(e -> this.context.putLabel(e.getKey(), e.getValue()));
        return Mono.fromFuture(future);
    }

    public synchronized <T> Mono<T> triggerAsync(Function<WorkContext, Mono<Object>> handler) {
        if (!isGate && this.context == null) {
            return Mono.error(new WorkflowException("External trigger {0} not active yet", this.getClass().getName()));
        }
        WorkContext ctx = this.context == null ? new WorkContext().clone(true) : this.context;
        Mono<Object> out = handler.apply(ctx);
        AtomicReference<Status> reference = new AtomicReference<>(Status.COMPLETED);

        return out.map(o -> (T) mapOutCome(o, reference)).doOnSuccess(o -> future.complete(WorkStatus.publish(reference.get(), ctx)));
    }

    public synchronized <T> T trigger(Function<WorkContext, Object> handler) {
        if (!isGate && this.context == null) {
            throw new WorkflowException("External trigger {0} not active yet", this.getClass().getName());
        }
        WorkContext ctx = this.context == null ? new WorkContext().clone(true) : this.context;
        Object out = handler.apply(ctx);
        AtomicReference<Status> reference = new AtomicReference<>(Status.COMPLETED);
        T ret = mapOutCome(out, reference);
        checkWaitAndPublish(reference, ctx);
        return ret;
    }

    public void cancel(WorkContext context) {
        workUnit.log("external trigger cancel invoked");
        this.future.complete(WorkStatus.publish(Status.CANCELLED, context));
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

    private void checkWaitAndPublish(AtomicReference<Status> reference, WorkContext context) {
        if (reference.get() != Status.WAIT) {
            future.complete(WorkStatus.publish(reference.get(), context));
        }
    }
}
