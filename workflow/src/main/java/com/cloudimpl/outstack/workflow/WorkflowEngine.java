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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class WorkflowEngine {

    private Workflow mainFlow;
    private WorkContext context;
    private Map<String, ExternalTrigger> triggers;
    private Set<String> triggerNames;
    private transient BiFunction<String, WorkResult, Mono<WorkResult>> updateStateHandler;
    private transient Function<String, Mono<WorkResult>> stateSupplier;

    public WorkflowEngine() {
        this.triggers = new ConcurrentHashMap<>();
        this.triggerNames = new HashSet<>();
        this.updateStateHandler = this::dummyStateUpdater;
        this.stateSupplier = this::dummyStateSupplier;

    }

    public Mono<WorkResult> execute(Workflow workFlow, WorkContext context) {
        workFlow.setEngine(this);
        workFlow.setHandlers(updateStateHandler, stateSupplier);
        if (this.mainFlow != null || this.context != null) {
            return Mono.error(() -> new WorkflowException("workflow is already executed "));
        }
        this.mainFlow = workFlow;
        this.context = context;
        return run();
    }

    public <T> Mono<T> externalTrigger(String name, Function<WorkContext, T> handler) {
        ExternalTrigger trigger = this.triggers.get(name);
        if (trigger == null) {
            return Mono.error(() -> new WorkflowException("external trigger {0} not found", name));
        }
        return trigger.trigger(handler);
    }

    private Mono<WorkResult> run() {
        return mainFlow.execute(context);
    }

    protected void registerExternalTrigger(String name, ExternalTrigger trigger) {
        ExternalTrigger old = triggers.putIfAbsent(name, trigger);
        if (old != null) {
            throw new WorkflowException("duplicate name {0} found in external trigger", name);
        }
    }

    protected void checkTriggerDuplicate(String name) {
        triggerNames.add(name);
    }

    private Mono<WorkResult> dummyStateUpdater(String id, WorkResult result) {
        return Mono.just(result);
    }

    private Mono<WorkResult> dummyStateSupplier(String id) {
        return Mono.empty();
    }

}
