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
import com.cloudimpl.outstack.common.MonoFuture;
import com.cloudimpl.outstack.core.CloudUtil;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class WorkUnit extends AbstractWork {

    private final Class<? extends Work> workUnit;
    private final String content;
    private MonoFuture<WorkStatus> watchFuture;
    protected CompletableFuture<WorkStatus> gateFuture;

    private WorkUnit(String id, String name, Class<? extends Work> work, String content) {
        super(id, name);
        System.out.println("workuni : " + id + " " + name);
        this.workUnit = work;
        this.content = content;
    }

    @Override
    public synchronized Mono<WorkStatus> execute(WorkContext context) {
        Optional<WorkStatus> status = loadWorkStatus();
        if (status.isPresent()) {
            return Mono.just(status.get());
        }
        WorkContext copy = isStateful() ? context.clone(false) : context.clone(true);
        Work workItem = GsonCodec.decode(workUnit, content);
        copy.setRRHandler(rrHandler);
        if (workItem instanceof ExternalTrigger) {
            ExternalTrigger.class.cast(workItem).init(this);
            getEngine().registerExternalTrigger(getName(), (ExternalTrigger) workItem);
        }
        Mono<WorkStatus> ret = workItem.execute(copy)
                .doOnNext(s -> mergeIfPossible(copy, s.getData()))
                .doOnNext(r -> copy.getStatus(id).set(r.getStatus()));
        if (workItem instanceof StatefullWork) {
            ret = ret.flatMap(r -> this.updateStateHandler.apply(getId(), WorkStatus.publish(r.getStatus(), copy)));
        }
        return ret
                .doOnSuccess(s -> removeActiveTrigger(workItem instanceof ExternalTrigger))
                .map(r -> WorkStatus.publish(isStateful() ? r.getStatus() : Status.COMPLETED, copy))
                .doOnNext(r -> log("done : {0}", r.getStatus()))
                .doOnNext(r -> cancelRestIfApplicable(r)).doOnError(e -> error(e, "WorkUnit {0} error", getName()));
    }

    private void mergeIfPossible(WorkContext dest, WorkContext source) {
        if (!dest.isImmutable() && source != null && dest != source) {
            dest.merge(source);
        }
    }

    private Optional<WorkStatus> loadWorkStatus() {
        return this.workStatusLoader.apply(getId());
    }

    private void cancelRestIfApplicable(WorkStatus status) {
        if (status.getStatus() == Status.CANCELLED) {
            getEngine().cancel();
        }
    }

    private boolean isStateful() {
        return StatefullWork.class.isAssignableFrom(workUnit);
    }

    private void removeActiveTrigger(boolean isTrigger) {
        if (isTrigger) {
            getEngine().removeActiveTrigger(getName());
        }
    }

    @Override
    protected void setEngine(WorkflowEngine engine) {
        super.setEngine(engine);
        if (ExternalTrigger.class.isAssignableFrom(workUnit)) {
            engine.checkTriggerDuplicate(this.getName());
        }
        if (GateTrigger.class.isAssignableFrom(workUnit)) {
            this.gateFuture = new CompletableFuture<>();
            GateTrigger gate = new GateTrigger();
            gate.init(this);
            getEngine().registerExternalTrigger(getName(), (ExternalTrigger) gate);
        }
    }

    public static Builder of(String name, Work work) {

        return new Builder(name, work);
    }

    public static WorkUnit waitFor(String name, Map<String, String> labels) {
        return WorkUnit.of(name, new ExternalTrigger().putLabel(labels)).build();
    }

    public static WorkUnit waitFor(String name) {
        return waitFor(name, Collections.EMPTY_MAP);
    }

    public static WorkUnit gateFor(String name, Map<String, String> labels) {
        return WorkUnit.of(name, new GateTrigger().putLabel(labels)).build();
    }

    public static WorkUnit gateFor(String name) {
        return gateFor(name, Collections.EMPTY_MAP);
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
