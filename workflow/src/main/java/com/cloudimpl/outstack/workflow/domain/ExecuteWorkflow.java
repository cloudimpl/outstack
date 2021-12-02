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

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.runtime.AsyncEntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.Workflow;
import com.cloudimpl.outstack.workflow.WorkflowEngine;
import com.google.gson.JsonObject;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class ExecuteWorkflow extends AsyncEntityCommandHandler<WorkflowEntity, Command, Object>{

    @Override
    protected Mono<Object> execute(EntityContext<WorkflowEntity> context, Command command) {
        WorkflowEntity entity = context.<WorkflowEntity>asAsyncEntityContext().getEntityById(command.getWorkflowId()).orElseThrow(()->new ValidationErrorException("invalid workflow id: "+command.getWorkflowId()));
        if(entity.getStatus() == Work.Status.COMPLETED)
        {
            return Mono.error(()->new ValidationErrorException("workflow id: "+command.getWorkflowId() + " completed"));
        }
        JsonObject json = GsonCodec.toJsonElement(entity.getContent()).getAsJsonObject();
      //  Workflow workflow = Work.fromJson(json).asWorkflow();
     //   WorkflowEngine engine = new WorkflowEngine();
       // engine.execute(workflow, context);
       return null;
    }
    
}
