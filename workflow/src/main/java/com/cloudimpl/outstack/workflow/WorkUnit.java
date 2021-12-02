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

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.core.CloudUtil;
import com.cloudimpl.outstack.runtime.util.Util;
import com.cloudimpl.outstack.workflow.domain.WorkflowEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class WorkUnit extends AbstractWork {

    private final Class<? extends Work> workUnit;
    private final String name;
    private final List<Param> params;

    private WorkUnit(String id, String name, Class<? extends Work> work, List<Param> params) {
        super(id);
        this.name = name;
        this.workUnit = work;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    @Override
    public Mono<WorkResult> execute(WorkContext context) {
        Class[] types = params.stream().map(p -> CloudUtil.classForName(p.getType())).toArray(Class[]::new);
        Object[] args = params.stream().map(p -> p.getItem()).toArray(Object[]::new);
        Work workItem = Util.createObject(workUnit, new Util.VarArg<>(types), new Util.VarArg<>(args));
        if (workItem instanceof ExternalTrigger) {
            getEngine().registerExternalTrigger(getName(), (ExternalTrigger) workItem);
        }
        if (workItem instanceof StatefullWork) {
            return this.stateSupplier.apply(getId()).flatMap(this::emitResultIfCompleted)
                    .switchIfEmpty(Mono.defer(() -> workItem.execute(context).flatMap(r -> this.updateStateHandler.apply(getId(), r))));
        }
        return workItem.execute(context);
    }

    @Override
    protected void setEngine(WorkflowEngine engine) {
        super.setEngine(engine);
        if (workUnit.isAssignableFrom(ExternalTrigger.class)) {
            engine.checkTriggerDuplicate(this.getName());
        }
    }

    private Mono<WorkResult> emitResultIfCompleted(WorkResult result) {
        if (result.getStatus() == Status.COMPLETED) {
            return Mono.just(result);
        }
        return Mono.empty();
    }

    public static Builder of(String name, Class<? extends Work> work) {

        return new Builder(name, work);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("workflowType", WorkUnit.class.getName());
        json.addProperty("workUnit", workUnit.getName());
        JsonArray arr = new JsonArray();
        params.forEach(p -> arr.add(p.toJson()));
        json.add("params", arr);
        return json;
    }

    public static WorkUnit fromJson(JsonObject json) {
        String workUnit = json.get("workUnit").getAsString();
        JsonArray arr = json.getAsJsonArray("params");
        List<Param> params = new LinkedList<>();
        arr.forEach(el -> params.add(Param.fromJson(el.getAsJsonObject())));
        return new WorkUnit(json.get("id").getAsString(), json.get("name").getAsString(), CloudUtil.classForName(workUnit), params);
    }

    public static class Builder {

        private Class<? extends Work> work;
        private String name;
        private final List<Param> params = new LinkedList<>();

        public Builder(String name, Class<? extends Work> work) {
            this.name = name;
            this.work = work;
        }

        public Builder withParam(Object... param) {

            Arrays.asList(param).forEach(p -> this.params.add(new Param(p, p.getClass().getName())));
            return this;
        }

        public WorkUnit build() {
            return new WorkUnit(Work.generateId(), name, work, params);
        }
    }

    public static final class Param {

        private final Object item;
        private final String type;

        public Param(Object item, String type) {
            this.item = item;
            this.type = type;
        }

        public Object getItem() {
            return item;
        }

        public String getType() {
            return type;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.add("item", GsonCodec.encodeToJson(item));
            json.addProperty("type", type);
            return json;
        }

        public static Param fromJson(JsonObject json) {
            Class type = CloudUtil.classForName(json.getAsJsonPrimitive("type").getAsString());
            Object item = GsonCodec.decode(type, json.get("item").toString());
            return new Param(item, json.getAsJsonPrimitive("type").getAsString());
        }
    }
}
