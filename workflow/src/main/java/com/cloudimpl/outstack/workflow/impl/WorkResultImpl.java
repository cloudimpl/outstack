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
package com.cloudimpl.outstack.workflow.impl;

import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkResult;
import com.cloudimpl.outstack.workflow.WorkflowException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class WorkResultImpl implements WorkResult{
    private Map<String,WorkContext> contexts = new HashMap<>();
    private final String name;

    public WorkResultImpl(String name) {
        this.name = name;
    }

    public WorkResultImpl(String name,Collection<WorkContext> contexts) {
        this.name = name;
        this.contexts = contexts.stream().collect(Collectors.toMap(c->c.name(), c->c));
    }
    
    
    @Override
    public WorkContext getContext() {
        if(contexts.size() == 1)
        {
            return contexts.entrySet().stream().findFirst().get().getValue();
        }
        else{
            throw new WorkflowException("multiple contexts found for workflow {0}",name);
        }
    }

    @Override
    public WorkContext getContext(String name) {
        return contexts.get(name);
    }

    @Override
    public List<WorkContext> getContextList() {
        return new LinkedList<>(contexts.values());
    }
    
    
    
}
