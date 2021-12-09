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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author nuwan
 */
public class AtleastWorkFlow extends Workflow {

    private final List<AbstractWork> workUnits;
    private final int take;

    private AtleastWorkFlow(String id, String name, List<AbstractWork> workUnits, int take) {
        super(id, name);
        this.workUnits = workUnits;
        this.take = take;
    }

    @Override
    public Mono<WorkStatus> execute(WorkContext context) {
        WorkContext copy = context.clone(false);
        log("started");
        return Flux.fromIterable(workUnits)
                .parallel(Runtime.getRuntime().availableProcessors())
                .runOn(Schedulers.parallel())
                .flatMap(wk -> retryWrap(wk, copy))
                .sequential()
                .take(take)
                .collectList()
                .map(l -> merge(WorkStatus.publish(Status.COMPLETED, copy), l)).doOnNext(r -> log("done : {0}", r.getStatus()));
    }

    @Override
    protected void setEngine(WorkflowEngine engine) {
        this.engine = engine;
        workUnits.forEach(w -> w.setEngine(engine));
    }

    @Override
    protected void setHandlers(BiFunction<String, WorkStatus, Mono<WorkStatus>> updateStateHandler, Consumer<Object> autoWireHandler, Function<String, Optional<WorkStatus>> workStatusLoader) {
        super.setHandlers(updateStateHandler, autoWireHandler, workStatusLoader);
        workUnits.forEach(w -> w.setHandlers(updateStateHandler, autoWireHandler, workStatusLoader));
    }

    protected static AtleastWorkFlow.AtleastStep name(String name) {
        AtleastWorkFlow.Builder builder = new AtleastWorkFlow.Builder(name);
        return new AtleastWorkFlow.AtleastStep(builder);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", getId());
        json.addProperty("workflowType", AtleastWorkFlow.class.getName());
        json.addProperty("name", name);
        json.addProperty("take", take);
        JsonArray arr = new JsonArray();
        workUnits.stream().forEach(w -> arr.add(w.toJson()));
        json.add("workUnits", arr);
        return json;
    }

    public static AtleastWorkFlow fromJson(JsonObject json) {
        JsonArray arr = json.getAsJsonArray("workUnits");
        List<AbstractWork> workunits = new LinkedList<>();
        arr.forEach(w -> workunits.add(AbstractWork.fromJson(w.getAsJsonObject())));
        AtleastWorkFlow workflow = new AtleastWorkFlow(json.get("id").getAsString(), json.get("name").getAsString(), workunits, json.get("take").getAsInt());
        return workflow;
    }

    public static final class Builder {

        private final List<AbstractWork> works = new LinkedList<>();
        private int take = 0;
        private String name;

        private Builder(String name) {
            this.name = name;
        }

    }

    public static final class AtleastStep {

        private final Builder builder;

        private AtleastStep(Builder builder) {
            this.builder = builder;
        }

        public FromStep take(int take) {
            this.builder.take = take;
            return new FromStep(builder);
        }
    }

    public static final class FromStep {

        private final Builder builder;

        private FromStep(Builder builder) {
            this.builder = builder;
        }

        public FromStep from(AbstractWork... work) {
            Arrays.asList(work).forEach(w -> this.builder.works.add(w));
            return this;
        }

        public AtleastWorkFlow build() {
            return new AtleastWorkFlow(Work.generateId(), builder.name, builder.works, builder.take);
        }
    }
}
