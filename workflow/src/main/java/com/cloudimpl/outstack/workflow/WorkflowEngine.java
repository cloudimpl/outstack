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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.Function3;
import reactor.function.Function4;
import reactor.retry.Retry;

/**
 *
 * @author nuwan
 */
@Slf4j
public class WorkflowEngine {

    private Workflow mainFlow;
    private WorkContext context;
    private Map<String, ExternalTrigger> triggers;
    private Set<String> triggerNames;

    private Function4<String, String, String, WorkStatus, Mono<WorkStatus>> updateStateHandler;
    private BiFunction<String, String, Optional<WorkStatus>> workStatusLoader;
    private BiFunction<String, Object, Mono> rrHandler;
    private String id;
    private Set<String> activeTriggers;
    private FluxProcessor<MonoFuture> dbWriter;
    private Disposable dbWriterHnd;
    private String tenantId;
    private Work.Status status;
    private Disposable workflowHnd;

    public WorkflowEngine(String id) {
        this(id, null, WorkflowEngine::dummyStateUpdater, null, WorkflowEngine::dummyWorkStatusLoader);
    }

    public WorkflowEngine(String id, String tenantId, Function4<String, String, String, WorkStatus, Mono<WorkStatus>> updateStateHandler, BiFunction<String, Object, Mono> rrHandler, BiFunction<String, String, Optional<WorkStatus>> workStatusLoader) {
        this.id = id;
        this.tenantId = tenantId;
        this.triggers = new ConcurrentHashMap<>();
        this.triggerNames = new HashSet<>();
        this.activeTriggers = new ConcurrentSkipListSet<>();
        this.updateStateHandler = updateStateHandler;
        this.rrHandler = rrHandler;
        this.status = Work.Status.PENDING;
        this.workStatusLoader = workStatusLoader;
    }

    public String getId() {
        return id;
    }

    public Mono<WorkStatus> execute(Workflow workFlow) {
        workFlow.setEngine(this);
        workFlow.setHandlers(this::stateUpdater, rrHandler, this::workStatusLoader);
        if (this.mainFlow != null || this.context != null) {
            return Mono.error(() -> new WorkflowException("workflow is already executed "));
        }
        this.status = Work.Status.RUNNING;
        this.context = new WorkContext();
        this.mainFlow = workFlow;
        dbWriter = new FluxProcessor<>("workflow engine " + id);
        dbWriterHnd = dbWriter.flux("workflow engine " + id)
                .publishOn(Schedulers.parallel())
                .flatMap(s -> writeToDB((String) s.getParam(0), (WorkStatus) s.getParam(1))
                .doOnNext(k -> s.submit(k)), 1)
                .doOnTerminate(() -> mainFlow.log("db writer closing down on terminate"))
                .doOnCancel(() -> mainFlow.log("db writer closing down on cancel"))
                .subscribe();
        MonoFuture<WorkStatus> future = MonoFuture.create();

        workflowHnd = run()
                .doOnSuccess(s
                        -> {
                    future.submit(s);
                    log.info("workflow engine {} succesfully completed", this.id);
                }
                ).doOnCancel(() -> {
                    future.submit(WorkStatus.publish(Work.Status.CANCELLED));
                    log.info("workflow engine {} cancelled", this.id);
                })
                .subscribe();
        return future.get().flatMap(s -> stateUpdater(this.id, s)).doOnNext(s -> dbWriterHnd.dispose());
    }

    public Work.Status getStatus() {
        return this.status;
    }

    public void cancel() {
        this.workflowHnd.dispose();
    }

    public <T> Mono<T> executeAsync(String name, Function<WorkContext, Mono<Object>> handler) {
        ExternalTrigger trigger = this.triggers.get(name);
        if (trigger == null) {
            return Mono.error(() -> new WorkflowException("external trigger {0} not found", name));
        }
        if (trigger.isGate()) {
            throw new WorkflowException("wait trigger {0} not found .found {1}", name, trigger.getClass().getName());
        }
        return trigger.triggerAsync(handler);
    }

    public <T> T execute(String name, Function<WorkContext, Object> handler) {
        ExternalTrigger trigger = this.triggers.get(name);
        if (trigger == null) {
            throw new WorkflowException("external trigger {0} not found", name);
        }
        if (trigger.isGate()) {
            throw new WorkflowException("wait trigger {0} not found .found {1}", name, trigger.getClass().getName());
        }
        return trigger.trigger(handler);
    }

    public void openGate(String name, Consumer<WorkContext> consumer) {
        ExternalTrigger trigger = this.triggers.get(name);
        if (trigger == null) {
            throw new WorkflowException("gate trigger {0} not found", name);
        }
        if (!trigger.isGate()) {
            throw new WorkflowException("gate trigger {0} not found .found {1}", name, trigger.getClass().getName());
        }
        Function<WorkContext, Object> fun = c -> {
            consumer.accept(c);
            return "";
        };
        trigger.trigger(fun);
    }

    public Mono<Void> openGateAsync(String name, Consumer<WorkContext> consumer) {
        ExternalTrigger trigger = this.triggers.get(name);
        if (trigger == null) {
            return Mono.error(() -> new WorkflowException("get trigger {0} not found", name));
        }
        if (!trigger.isGate()) {
            return Mono.error(() -> new WorkflowException("gate trigger {0} not found .found {1}", name, trigger.getClass().getName()));
        }
        Function<WorkContext, Mono<Object>> fun = c -> {
            consumer.accept(c);
            return Mono.just(true);
        };
        return trigger.triggerAsync(fun).then();
    }

    public <T> Mono<T> executeAsyncNext(Function<WorkContext, Mono<Object>> handler) {
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
        if (trigger.isGate()) {
            return Mono.error(() -> new WorkflowException("gate trigger {0} not support anonymous execution", active.get(0)));
        }
        return trigger.triggerAsync(handler);
    }

    public <T> T executeNext(Function<WorkContext, Object> handler) {
        List<String> active = activeTriggers.stream().collect(Collectors.toList());

        if (active.isEmpty()) {
            throw new WorkflowException("no active external trigger found for workflow id {0}", id);
        }
        if (active.size() > 1) {
            throw new WorkflowException("more than 1 active external trigger found for workflow id {0} - [{1}]", id, active.stream().collect(Collectors.joining(",")));
        }
        ExternalTrigger trigger = this.triggers.get(active.get(0));
        if (trigger == null) {
            throw new WorkflowException("external trigger {0} not found", active.get(0));
        }
        if (trigger.isGate()) {
            throw new WorkflowException("gate trigger {0} not support anonymous execution", active.get(0));
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

    public static Mono<WorkStatus> dummyStateUpdater(String workflowId, String workId, String tenantId, WorkStatus result) {
        System.out.println("db write : " + workflowId + " workId : " + workId + "result : " + result.getStatus() + " context : " + result.getData());
        return Mono.just(result);
    }

    public static Optional<WorkStatus> dummyWorkStatusLoader(String workFlowId, String workId) {
        return Optional.empty();
    }

    public Optional<WorkStatus> workStatusLoader(String workId) {
        return this.workStatusLoader.apply(this.id, workId);
    }

    private Mono<WorkStatus> stateUpdater(String id, WorkStatus result) {
        if (id.equals(this.id)) {
            mainFlow.log("workflow engine completing , writing final state");
        }
        MonoFuture<WorkStatus> future = MonoFuture.create(id, result);
        this.dbWriter.add(future);
        return future.get();
    }

    private Mono<WorkStatus> writeToDB(String id, WorkStatus result) {
        return Mono.defer(() -> this.updateStateHandler.apply(this.id, id, tenantId, result))
                .doOnError(err -> log.error("Error writing to DB for workflow " + this.id, err))
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
