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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import reactor.core.publisher.Mono;
/**
 *
 * @author nuwan
 */
public class SequentialWorkflow extends Workflow {

    private final List<AbstractWork> workUnits;

    private SequentialWorkflow(String id, String name, List<AbstractWork> works) {
        super(id, name);
        this.workUnits = Collections.unmodifiableList(works);
    }

    @Override
    public Mono<WorkStatus> execute(WorkContext context) {
        log("exec started");
        Mono<WorkStatus> ret = null;
        for (AbstractWork flow : workUnits) {
            if (ret == null) {
                ret = retryWrap(flow, context);
            } else {
                ret = ret.flatMap(r -> retryWrap(flow, r.getData()));
            } 
        }
        return ret == null ? Mono.empty() : ret.doOnNext(r->log("done : {0}", r.getStatus()));
    }
    
    @Override
    protected void setEngine(WorkflowEngine engine) {
        this.engine = engine;
        this.workUnits.forEach(w -> w.setEngine(engine));
    }

    @Override
    protected void setHandlers(BiFunction<String, WorkStatus, Mono<WorkStatus>> updateStateHandler, BiFunction<String, Object, Mono> rrHandler,Function<String, Optional<WorkStatus>> workStatusLoader) {
        super.setHandlers(updateStateHandler, rrHandler, workStatusLoader);
        this.workUnits.forEach(w -> w.setHandlers(updateStateHandler, rrHandler,workStatusLoader));
    }

    public static final SequentialWorkflow.ExecuteStep name(String name) {
        Builder builder = new Builder(name);
        return new ExecuteStep(builder);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", getId());
        json.addProperty("workflowType", SequentialWorkflow.class.getName());
        json.addProperty("name", name);
        JsonArray arr = new JsonArray();
        workUnits.stream().forEach(w -> arr.add(w.toJson()));
        json.add("workUnits", arr);
        return json;
    }

    public static SequentialWorkflow fromJson(JsonObject json) {
        JsonArray arr = json.getAsJsonArray("workUnits");
        List<AbstractWork> workunits = new LinkedList<>();
        arr.forEach(w -> workunits.add(AbstractWork.fromJson(w.getAsJsonObject())));
        SequentialWorkflow workflow = new SequentialWorkflow(json.get("id").getAsString(), json.get("name").getAsString(), workunits);
        return workflow;
    }

    public static final class Builder {

        private String name;
        private final List<AbstractWork> works = new LinkedList<>();

        public Builder(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public static final class ExecuteStep {

        private final Builder builder;

        public ExecuteStep(Builder builder) {
            this.builder = builder;
        }

        public final SequentialWorkflow.ThenStep execute(AbstractWork work) {
            return new ThenStep(builder).then(work);
        }

    }

    public static final class ThenStep {

        private final Builder builder;

        public ThenStep(Builder builder) {
            this.builder = builder;
        }

        public ThenStep then(AbstractWork work) {
            this.builder.works.add(work);
            return this;
        }

        public SequentialWorkflow build() {
            return new SequentialWorkflow(Work.generateId(), this.builder.name, this.builder.works);
        }
    }
}
