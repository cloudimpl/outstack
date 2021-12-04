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
    public synchronized Mono<WorkStatus> execute(WorkContext context) {
        if (!context.getStatus(id).compareAndSet(Status.PENDING, Status.RUNNING)) {
            return Mono.just(WorkStatus.publish(context.getStatus(id).get(), context)).doOnNext(r->log("done : {0}", r.getStatus()));
        }
        WorkContext copy = context.clone();
        Work workItem = GsonCodec.decode(workUnit, content);
        copy.setRRHandler(rrHandler);
        if (workItem instanceof ExternalTrigger) {
            ExternalTrigger.class.cast(workItem).init(this);
            getEngine().registerExternalTrigger(getName(), (ExternalTrigger) workItem);
            getEngine().addActiveTrigger(getName());
        }
        Mono<WorkStatus> ret = workItem.execute(copy)
                .doOnNext(r -> copy.getStatus(id).compareAndSet(Status.RUNNING, r.getStatus()));
        if (workItem instanceof StatefullWork) {
            ret = ret.flatMap(r -> this.updateStateHandler.apply(getId(), r));
        }
        return ret.doOnSuccess(s -> removeActiveTrigger(workItem instanceof ExternalTrigger)).map(r -> WorkStatus.publish(r.getStatus(), copy)).doOnNext(r->log("done : {0}", r.getStatus()));
    }

    private void removeActiveTrigger(boolean isTrigger) {
        if (isTrigger) {
            getEngine().removeActiveTrigger(getName());
        }
    }

    @Override
    protected void setEngine(WorkflowEngine engine) {
        super.setEngine(engine);
        if (workUnit.isAssignableFrom(ExternalTrigger.class)) {
            engine.checkTriggerDuplicate(this.getName());
        }
    }

    @Override
    public synchronized void cancel(WorkContext context) {
        super.cancel(context);
        if (workUnit.isAssignableFrom(ExternalTrigger.class)) {
            ExternalTrigger trigger = getEngine().getExternalTrigger(getName());
            if (trigger != null) {
                trigger.cancel(context);
            }
        }
    }

    public static Builder of(String name, Work work) {

        return new Builder(name, work);
    }

    public static WorkUnit waitFor(String name) {
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

        private Work work;
        private String name;

        public Builder(String name, Work work) {
            this.name = name;
            this.work = work;
        }

        public WorkUnit build() {

            return new WorkUnit(Work.generateId(), name, work.getClass(), GsonCodec.encode(work));
        }
    }
}
