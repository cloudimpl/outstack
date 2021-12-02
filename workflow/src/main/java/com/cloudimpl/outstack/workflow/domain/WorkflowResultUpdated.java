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
import com.cloudimpl.outstack.workflow.WorkResult;

/**
 *
 * @author nuwan
 */
public class WorkflowResultUpdated extends Event<WorkflowEntity>{
    private final String workflowId;
    private final String workId;
    private final WorkResult result;

    public WorkflowResultUpdated(String workflowId, String workId, WorkResult result) {
        this.workflowId = workflowId;
        this.workId = workId;
        this.result = result;
    }

    public String getWorkId() {
        return workId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public WorkResult getResult() {
        return result;
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
