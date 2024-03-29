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
package com.cloudimpl.outstack.workflow.domain;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.core.CloudUtil;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ITenantOptional;
import com.cloudimpl.outstack.runtime.domainspec.Id;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.workflow.Work.Status;
import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkStatus;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author nuwansa
 */
@EntityMeta(plural = "workflows", version = "v1")
public class WorkflowEntity extends RootEntity implements ITenantOptional {

    @Id
    private String workflowId;
    private String content;
    private Status status;
    private String tenantId;
    private Map<String, WorkContext> results;

    public WorkflowEntity(String workflowId, String tenantId) {
        this.workflowId = workflowId;
        this.tenantId = tenantId;
        this.status = Status.PENDING;
        this.results = new LinkedHashMap<>();
    }

    @Override
    public String entityId() {
        return workflowId;
    }

    public Status getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public Optional<WorkStatus> getStatus(String workId) {
        WorkContext ctx = results.get(workId);
        if (ctx != null) {
            return Optional.of(WorkStatus.publish(ctx.getStatus(workId).get(), ctx));
        }
        return Optional.empty();
    }

    private void applyEvent(WorkflowCreated evt) {
        this.workflowId = evt.getWorkflowId();
        this.content = evt.getContent();
        this.status = evt.getStatus();
    }

    private void applyEvent(WorkflowResultUpdated evt) {
        this.results.put(evt.getWorkId(), evt.getContext());
    }

    private void applyEvent(WorkflowCompleted evt) {
        this.status = evt.getStatus();
    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "WorkflowCreated": {
                applyEvent((WorkflowCreated) event);
                break;
            }
            case "WorkflowResultUpdated": {
                applyEvent((WorkflowResultUpdated) event);
                break;
            }
            case "WorkflowCompleted": {
                applyEvent((WorkflowCompleted) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "name";
    }

}
