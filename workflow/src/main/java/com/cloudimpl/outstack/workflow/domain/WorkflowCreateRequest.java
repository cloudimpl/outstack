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

/**
 *
 * @author nuwan
 */
public class WorkflowCreateRequest extends Command{
    private final String content;

    public WorkflowCreateRequest(Builder builder) {
        super(builder);
        this.content = builder.content;
    }

    public String getContent() {
        return content;
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder
    {
        private String content;
        
        public Builder withContent(String content)
        {
            this.content = content;
            return this;
        }
        
        @Override
        public WorkflowCreateRequest build()
        {
            return new WorkflowCreateRequest(this);
        }
    }
}
