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

import com.cloudimpl.outstack.common.RetryUtil;
import com.cloudimpl.outstack.runtime.Context;
import com.google.gson.JsonObject;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;
import reactor.retry.RetryContext;

/**
 *
 * @author nuwan
 */
@Slf4j
public abstract class AbstractWork implements Work {

    protected final String id;
    protected final String name;
    protected WorkflowEngine engine;
    protected BiFunction<String, WorkStatus, Mono<WorkStatus>> updateStateHandler;
    protected Function<String, Optional<WorkStatus>> workStatusLoader;
    protected BiFunction<String, Object, Mono> rrHandler;
    
    public AbstractWork(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    protected void setEngine(WorkflowEngine engine) {
        this.engine = engine;
    }

    protected WorkflowEngine getEngine() {
        return this.engine;
    }

    public Mono<WorkStatus> run(WorkContext context)
    {
        return execute(context);
    }
  
    
    public void log(String format, Object... args) {
        String msg = MessageFormat.format(format, args);
        log.info("workflow {}:{}:{} -> {} ", engine.getId(), this.getClass().getSimpleName(), name, msg);
    }

    protected Mono<WorkStatus> retryWrap(Work work, WorkContext context) {
        return Mono.fromSupplier(() -> work).flatMap(f -> f.execute(context))
                .doOnError(err -> error(err, "error:"))
                .retryWhen(RetryUtil.wrap(Retry.onlyIf(c -> isRetryable(c,context)).exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60))));
    }
    
    
    private boolean isRetryable(RetryContext retryContext,WorkContext context) {
        return getEngine().getStatus() == Status.RUNNING;
    }
    
    public void error(Throwable thr,String format,Object... args)
    {
        String msg = MessageFormat.format(format, args);
        log.error("workflow "+engine.getId()+":"+this.getClass().getSimpleName()+":"+name+" -> "+msg,thr);
    }
    
    protected void setHandlers(BiFunction<String, WorkStatus, Mono<WorkStatus>> updateStateHandler, BiFunction<String, Object, Mono> rrHandler,Function<String, Optional<WorkStatus>> workStatusLoader) {
        this.updateStateHandler = updateStateHandler;
        this.rrHandler = rrHandler;
        this.workStatusLoader = workStatusLoader;
    }

    public static AbstractWork fromJson(JsonObject json) {
        String workFlow = json.get("workflowType").getAsString();
        switch (workFlow) {
            case "com.cloudimpl.outstack.workflow.SequentialWorkflow": {
                return SequentialWorkflow.fromJson(json);
            }
            case "com.cloudimpl.outstack.workflow.WorkUnit": {
                return WorkUnit.fromJson(json);
            }
            case "com.cloudimpl.outstack.workflow.ParallelWorkflow": {
                return ParallelWorkflow.fromJson(json);
            }
            case "com.cloudimpl.outstack.workflow.ConditionalWorkflow": {
                return ConditionalWorkflow.fromJson(json);
            }
            default: {
                throw new WorkflowException("unknown workflow type {}", workFlow);
            }
        }
    }
}
