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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Retry;

/**
 *
 * @author nuwan
 */
public class ParallelWorkflow extends Workflow {

    private final List<AbstractWork> workUnits;

    public ParallelWorkflow(String id, String name, List<AbstractWork> works) {
        super(id,name);
        this.workUnits = Collections.unmodifiableList(works);
    }

    @Override
    public Mono<WorkResult> execute(WorkContext context) {
        WorkResult result = new WorkResult(Status.PENDING, context);
        WorkContext copy = context.clone();
        return Flux.fromIterable(workUnits)
                .parallel(Runtime.getRuntime().availableProcessors())
                .runOn(Schedulers.parallel())
                .flatMap(wk -> Mono.fromSupplier(() -> wk).flatMap(w -> w.execute(copy.clone())
                .doOnError(err -> Throwable.class.cast(err).printStackTrace())
                .retryWhen(RetryUtil.wrap(Retry.onlyIf(c -> result.getStatus() == Status.PENDING).exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60))))))
                .sequential()
                .collectList().map(l -> merge(result, l));
    }

    @Override
    protected void setEngine(WorkflowEngine engine) {
        this.engine = engine;
        workUnits.forEach(w -> w.setEngine(engine));
    }

    
    @Override
    protected void setHandlers(BiFunction<String,WorkResult,Mono<WorkResult>> updateStateHandler,Function<String,Mono<WorkResult>> stateSupplier,BiFunction<String,Object,Mono> rrHandler)
    {
        this.updateStateHandler = updateStateHandler;
        this.stateSupplier = stateSupplier;
        workUnits.forEach(w -> w.setHandlers(updateStateHandler, stateSupplier,rrHandler));
    }
    
    public static ExecuteStep name(String name) {
        Builder builder = new Builder(name);
        return new ExecuteStep(builder);
    }

    private WorkResult merge(WorkResult result, List<WorkResult> contextList) {
        contextList.stream().forEach(c -> result.getContext().merge(c.getContext()));
        return result;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", getId());
        json.addProperty("workflowType", ParallelWorkflow.class.getName());
        json.addProperty("name", name);
        JsonArray arr = new JsonArray();
        workUnits.stream().forEach(w -> arr.add(w.toJson()));
        json.add("workUnits", arr);
        return json;
    }

    public static ParallelWorkflow fromJson(JsonObject json) {
        JsonArray arr = json.getAsJsonArray("workUnits");
        List<AbstractWork> workunits = new LinkedList<>();
        arr.forEach(w -> workunits.add(AbstractWork.fromJson(w.getAsJsonObject())));
        ParallelWorkflow workflow = new ParallelWorkflow(json.get("id").getAsString(), json.get("name").getAsString(), workunits);
        return workflow;
    }

    public static final class Builder {

        private final List<AbstractWork> works = new LinkedList<>();
        private String name;

        public Builder(String name) {
            this.name = name;
        }

    }

    public static final class ExecuteStep {

        private final Builder builder;

        public ExecuteStep(Builder builder) {
            this.builder = builder;
        }

        public ExecuteStep execute(AbstractWork... works) {
            Arrays.asList(works).forEach(w -> this.builder.works.add(w));
            return this;
        }

        public ParallelWorkflow build() {
            return new ParallelWorkflow(Work.generateId(), this.builder.name, this.builder.works);
        }
    }
}
