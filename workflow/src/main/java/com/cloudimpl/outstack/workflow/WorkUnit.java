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
import com.google.gson.JsonObject;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class WorkUnit extends AbstractWork {

    private final Class<? extends Work> workUnit;
    private final String content;

    private WorkUnit(String id, String name, Class<? extends Work> work, String content) {
        super(id, name);
        this.workUnit = work;
        this.content = content;
    }

    @Override
    public Mono<WorkStatus> execute(WorkContext context) {
        if (!context.getStatus(id).compareAndSet(Status.PENDING, Status.RUNNING)) {
            return Mono.just(new WorkResult(context.getStatus(id).get(), context));
        }

        Work workItem = GsonCodec.decode(workUnit, content);

        context.setRRHandler(rrHandler);
        if (workItem instanceof ExternalTrigger) {
            getEngine().registerExternalTrigger(getName(), (ExternalTrigger) workItem);
        }
        Mono<WorkStatus> ret = workItem.execute(context).doOnNext(r->context.getStatus(id).set(Status.COMPLETED));
        if (workItem instanceof StatefullWork) {
            ret = ret.flatMap(r->this.updateStateHandler.apply(getId(), r));
        }
        return ret;
    }

    @Override
    protected void setEngine(WorkflowEngine engine) {
        super.setEngine(engine);
        if (workUnit.isAssignableFrom(ExternalTrigger.class)) {
            engine.checkTriggerDuplicate(this.getName());
        }
    }

    public static Builder of(String name, Work work) {

        return new Builder(name, work.getClass(), GsonCodec.encode(work));
    }

    public static WorkUnit waitFor(String name){
        return WorkUnit.of(name, new ExternalTrigger()).build();
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

        public Builder(String name, Class<? extends Work> work, String content) {
            this.name = name;
            this.workType = work;
            this.content = content;
        }

        public WorkUnit build() {
            return new WorkUnit(Work.generateId(), name, workType, content);
        }
    }
}
