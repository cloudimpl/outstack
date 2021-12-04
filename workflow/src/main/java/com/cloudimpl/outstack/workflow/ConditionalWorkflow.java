/*
 * Copyright 2021 nuwansa.
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

import com.cloudimpl.outstack.runtime.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwansa
 */
@Slf4j
public class ConditionalWorkflow extends Workflow {

    private WorkResult prevWorkResult;
    private final Class<? extends WorkPredicate> predicateType;
    private final AbstractWork then;
    private final AbstractWork otherwise;

    public ConditionalWorkflow(String id, String name, Class<? extends WorkPredicate> predicate, AbstractWork then, AbstractWork otherwise) {
        super(id, name);
        this.predicateType = predicate;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public Mono<WorkResult> execute(WorkContext context) {
        
        if (!context.getStatus(id).compareAndSet(Status.PENDING, Status.RUNNING)) {
            return Mono.just(new WorkResult(context.getStatus(id).get(), context));
        }
        
        WorkPredicate predicate = Util.createObject(this.predicateType, new Util.VarArg<>(), new Util.VarArg<>());
        log("started");
        Mono<WorkResult> ret;
        if (predicate.apply(prevWorkResult)) {
            log("then route initiated");
            ret =  then.execute(context);
        } else {
            log("othewise route initiated");
            ret =  otherwise.execute(context);
        }
        return ret.doOnNext(r->context.getStatus(id).set(Status.COMPLETED));
    }

    @Override
    public void cancel(WorkContext context) {
        super.cancel(context);
        this.then.cancel(context);
        this.otherwise.cancel(context);
    }

    public ConditionalWorkflow.Builder name(String name) {
        return new Builder(name);
    }

    protected void setPrevWorkResult(WorkResult workResult) {
        this.prevWorkResult = workResult;
    }

    @Override
    protected void setEngine(WorkflowEngine engine) {
        this.engine = engine;
        this.then.setEngine(engine);
        this.otherwise.setEngine(engine);
    }

    @Override
    protected void setHandlers(BiFunction<String, WorkResult, Mono<WorkResult>> updateStateHandler,  BiFunction<String, Object, Mono> rrHandler) {
        this.updateStateHandler = updateStateHandler;
        this.then.setHandlers(updateStateHandler, rrHandler);
        this.otherwise.setHandlers(updateStateHandler, rrHandler);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", getId());
        json.addProperty("workflowType", ConditionalWorkflow.class.getName());
        json.addProperty("name", name);
        json.addProperty("predicateType", predicateType.getName());
        json.add("then", then.toJson());
        json.add("otherwise", otherwise.toJson());
        return json;
    }

    public static ConditionalWorkflow fromJson(JsonObject json) {
        return new ConditionalWorkflow(json.get("id").getAsString(), json.get("name").getAsString(), Util.classForName(json.get("predicateType").getAsString()), AbstractWork.fromJson(json.getAsJsonObject("then")), AbstractWork.fromJson(json.getAsJsonObject("otherwise")));
    }

    public static final class Builder {

        private final String name;
        private String predicateName;
        private AbstractWork then;
        private AbstractWork otherwise;

        public Builder(String name) {
            this.name = name;
        }

        public ThenStep when(String workPredicate) {
            return new ThenStep(this);
        }
    }

    public static final class ThenStep {

        private Builder builder;

        public ThenStep(Builder builder) {
            this.builder = builder;
        }

        public OtherwiseStep then(AbstractWork work) {
            this.builder.then = work;
            return new OtherwiseStep(builder);
        }

    }

    public static final class OtherwiseStep {

        private final Builder builder;

        public OtherwiseStep(Builder builder) {
            this.builder = builder;
        }

        public BuildStep otherwise(AbstractWork work) {
            builder.otherwise = work;
            return new BuildStep(builder);
        }
    }

    public static final class BuildStep {

        private final Builder builder;

        public BuildStep(Builder builder) {
            this.builder = builder;
        }

        public ConditionalWorkflow build() {
            return new ConditionalWorkflow(Work.generateId(), builder.name, WorkPredicate.from(builder.predicateName), builder.then, builder.otherwise);
        }

    }
}
