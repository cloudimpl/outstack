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
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.domain.WorkflowCreateRequest;
import com.cloudimpl.outstack.workflow.domain.WorkflowCreated;
import com.cloudimpl.outstack.workflow.domain.WorkflowEntity;
import java.util.UUID;

/**
 *
 * @author nuwan
 */
public class CreateWorkflow extends EntityCommandHandler<WorkflowEntity, WorkflowCreateRequest, CommandResponse> {

    @Override
    protected CommandResponse execute(EntityContext<WorkflowEntity> context, WorkflowCreateRequest command) {
        String id = "flow-" + UUID.randomUUID().toString();
        WorkflowEntity entity = context.create(id, new WorkflowCreated(id, command.getContent(), Work.Status.PENDING));
        return new CommandResponse("OK", id);
    }

}
