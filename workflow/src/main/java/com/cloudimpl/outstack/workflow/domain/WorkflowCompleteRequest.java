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

import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.workflow.Work;

/**
 *
 * @author nuwan
 */
public class WorkflowCompleteRequest extends Command {

    private final String workflowId;
    private final Work.Status status;

    public WorkflowCompleteRequest(Builder builder) {
        super(builder);
        this.workflowId = builder.workflowId;
        this.status = builder.status;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public Work.Status getStatus() {
        return status;
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder {

        private String workflowId;
        private Work.Status status;

        public Builder withWorkflowId(String id) {
            this.workflowId = id;
            return this;
        }

        public Builder withStatus(Work.Status status) {
            this.status = status;
            return this;
        }
        
        @Override
        public WorkflowCompleteRequest build() {
            return new WorkflowCompleteRequest(this);
        }

    }

}
