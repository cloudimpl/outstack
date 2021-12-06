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
package com.cloudimpl.outstack.workflow.component;

import com.cloudimpl.outstack.runtime.CommandResponse;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.workflow.domain.WorkflowEntity;
import com.cloudimpl.outstack.workflow.domain.WorkflowResultUpdated;
import com.cloudimpl.outstack.workflow.domain.WorkflowUpdateRequest;

/**
 *
 * @author nuwan
 */
public class UpdateWorkflow extends EntityCommandHandler<WorkflowEntity, WorkflowUpdateRequest, WorkflowEntity> {

    @Override
    protected WorkflowEntity execute(EntityContext<WorkflowEntity> context, WorkflowUpdateRequest command) {
        return context.<WorkflowEntity>asRootContext()
                .update(command.getWorkflowId(), new WorkflowResultUpdated(command.getWorkflowId(), command.getWorkId(), command.getWorkContext()));
    }

}
