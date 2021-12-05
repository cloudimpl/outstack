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

import com.cloudimpl.outstack.common.FluxProcessor;
import com.cloudimpl.outstack.common.MonoFuture;
import com.cloudimpl.outstack.common.RetryUtil;
import com.cloudimpl.outstack.workflow.component.UpdateWorkflow;
import com.cloudimpl.outstack.workflow.domain.WorkflowResultUpdated;
import com.cloudimpl.outstack.workflow.domain.WorkflowUpdateRequest;
import static com.cloudimpl.outstack.workflow.example.FluxTest.work;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import reactor.function.Function3;
import reactor.retry.Retry;

/**
 *
 * @author nuwan
 */
public class WorkflowEngine {

    private Workflow mainFlow;
    private WorkContext context;
    private Map<String, ExternalTrigger> triggers;
    private Set<String> triggerNames;

    private Function3<String, String, WorkStatus, Mono<WorkStatus>> updateStateHandler;
    private BiFunction<String, Object, Mono> rrHandler;
    private String id;
    private Set<String> activeTriggers;
    private FluxProcessor<MonoFuture> dbWriter;

    public WorkflowEngine(String id) {
        this(id, WorkflowEngine::dummyStateUpdater, null);
    }

    public WorkflowEngine(String id, Function3<String, String, WorkStatus, Mono<WorkStatus>> updateStateHandler, BiFunction<String, Object, Mono> rrHandler) {
        this.id = id;
        this.triggers = new ConcurrentHashMap<>();
        this.triggerNames = new HashSet<>();
        this.activeTriggers = new ConcurrentSkipListSet<>();
        this.updateStateHandler = updateStateHandler;
        this.rrHandler = rrHandler;
    }

    public String getId() {
        return id;
    }

    public void cancel() {
        this.mainFlow.cancel(context);
    }

    public Mono<WorkStatus> execute(Workflow workFlow) {
        workFlow.setEngine(this);
        workFlow.setHandlers(this::stateUpdater, rrHandler);
        if (this.mainFlow != null || this.context != null) {
            return Mono.error(() -> new WorkflowException("workflow is already executed "));
        }
        this.context = new WorkContext();
        this.mainFlow = workFlow;
        dbWriter = new FluxProcessor<>("workflow engine " + id);
        dbWriter.flux("workflow engine " + id)
                .flatMap(s -> writeToDB((String) s.getParam(0), (WorkStatus) s.getParam(1)).doOnNext(k -> s.submit(k)))
                .doOnNext(s -> System.out.println("out : " + s)).subscribe();
        return run();
    }

    public <T> Mono<T> execute(String name, Function<WorkContext, T> handler) {
        ExternalTrigger trigger = this.triggers.get(name);
        if (trigger == null) {
            return Mono.error(() -> new WorkflowException("external trigger {0} not found", name));
        }
        return trigger.trigger(handler);
    }

    public <T> Mono<T> executeNext(Function<WorkContext, T> handler) {
        List<String> active = activeTriggers.stream().collect(Collectors.toList());
        if (active.isEmpty()) {
            return Mono.error(() -> new WorkflowException("no active external trigger found for workflow id {0}", id));
        }
        if (active.size() > 1) {
            return Mono.error(() -> new WorkflowException("more than 1 active external trigger found for workflow id {0} - [{1}]", id, active.stream().collect(Collectors.joining(","))));
        }
        ExternalTrigger trigger = this.triggers.get(active.get(0));
        if (trigger == null) {
            return Mono.error(() -> new WorkflowException("external trigger {0} not found", active.get(0)));
        }
        return trigger.trigger(handler);
    }

    protected ExternalTrigger getExternalTrigger(String name) {
        return this.triggers.get(name);
    }

    private Mono<WorkStatus> run() {
        return mainFlow.execute(context);
    }

    protected void registerExternalTrigger(String name, ExternalTrigger trigger) {
        triggers.put(name, trigger);
        this.activeTriggers.add(name);
    }

    protected void checkTriggerDuplicate(String name) {
        if (!triggerNames.add(name)) {
            throw new WorkflowException("duplicate name {0} found in external trigger in workflow {1} ", name, id);
        }
    }

    public static Mono<WorkStatus> dummyStateUpdater(String workflowId, String workId, WorkStatus result) {
        return Mono.just(result);
    }

    private Mono<WorkStatus> stateUpdater(String id, WorkStatus result) {
        MonoFuture<WorkStatus> future = MonoFuture.create(id, result);
        this.dbWriter.add(future);
        return future.get();
    }

    private Mono<WorkStatus> writeToDB(String id, WorkStatus result) {
        return Mono.defer(() -> this.updateStateHandler.apply(this.id, id, result))
                .doOnError(err -> System.out.println("errr: " + err.getMessage()))
                .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(3), Duration.ofSeconds(60))));

    }

    protected void removeActiveTrigger(String name) {
        this.activeTriggers.remove(name);
        this.triggers.remove(name);
    }

//     private  Mono<String> retryWrap(MonoFuture future)
//    {
//        return Mono.defer(()->this.updateStateHandler.apply(id, u))
//                .doOnError(err->System.out.println("errr: "+err.getMessage()))
//               .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(3), Duration.ofSeconds(60))));
//    }
}
