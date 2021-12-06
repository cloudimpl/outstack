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
package com.cloudimpl.outstack.workflow.domain;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.WorkContext;

/**
 *
 * @author nuwan
 */
public class WorkflowCompleted extends Event<WorkflowEntity>{
    private final String workflowId;
    private final Work.Status status;

    public WorkflowCompleted(String workflowId, Work.Status status) {
        this.workflowId = workflowId;
        this.status = status;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public Work.Status getStatus() {
        return status;
    }
    
    @Override
    public Class<? extends Entity> getOwner() {
        return WorkflowEntity.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
       return WorkflowEntity.class;
    }

    @Override
    public String entityId() {
        return workflowId;
    }

    @Override
    public String rootEntityId() {
        return workflowId;
    }
    
}
