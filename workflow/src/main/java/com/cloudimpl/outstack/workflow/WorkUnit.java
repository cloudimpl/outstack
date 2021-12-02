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
    private final String content;

    private WorkUnit(String id, String name, Class<? extends Work> work, String content) {
        super(id,name);
        this.workUnit = work;
        this.content = content;
    }

    @Override
    public Mono<WorkResult> execute(WorkContext context) {
     
        Work workItem = GsonCodec.decode(workUnit, content);
        
        context.setRRHandler(rrHandler);
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

    public static Builder of(String name, Work work) {

        return new Builder(name, work.getClass(),GsonCodec.encode(work));
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("workflowType", WorkUnit.class.getName());
        json.addProperty("workUnit", workUnit.getName());
        json.addProperty("content", content);
        return json;
    }

    public static WorkUnit fromJson(JsonObject json) {
        String workUnit = json.get("workUnit").getAsString();
        return new WorkUnit(json.get("id").getAsString(), json.get("name").getAsString(), CloudUtil.classForName(workUnit), json.getAsJsonPrimitive("content").getAsString());
    }

    public static class Builder {

        private Class<? extends Work> workType;
        private String name;
        private String content;

        public Builder(String name, Class<? extends Work> work,String content) {
            this.name = name;
            this.workType = work;
            this.content = content;
        }
        
        public WorkUnit build() {
            return new WorkUnit(Work.generateId(), name, workType, content);
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
