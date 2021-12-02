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

import com.google.gson.JsonObject;
import java.util.function.BiFunction;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public abstract class AbstractWork implements Work {

    protected final String id;
    protected WorkflowEngine engine;
    protected   BiFunction<String,WorkResult,Mono<WorkResult>> updateStateHandler;
    protected  Function<String,Mono<WorkResult>> stateSupplier;
    
    public AbstractWork(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    
    
    protected void setEngine(WorkflowEngine engine)
    {
        this.engine = engine;
    }
    
    protected WorkflowEngine getEngine()
    {
        return this.engine;
    }
    
    protected void setHandlers(BiFunction<String,WorkResult,Mono<WorkResult>> updateStateHandler,Function<String,Mono<WorkResult>> stateSupplier)
    {
        this.updateStateHandler = updateStateHandler;
        this.stateSupplier = stateSupplier;
    }
    
    public static AbstractWork fromJson(JsonObject json) {
        String workFlow = json.get("workflowType").getAsString();
        switch (workFlow) {
            case "com.cloudimpl.outstack.workflow.SequentialWorkflow": {
                return SequentialWorkflow.fromJson(json);
            }
            case "com.cloudimpl.outstack.workflow.WorkUnit": {
                return WorkUnit.fromJson(json);
            }
            case "com.cloudimpl.outstack.workflow.ParallelWorkflow": {
                return ParallelWorkflow.fromJson(json);
            }
            case "com.cloudimpl.outstack.workflow.ConditionalWorkflow":{
                return ConditionalWorkflow.fromJson(json);
            }
            default: {
                throw new WorkflowException("unknown workflow type {}", workFlow);
            }
        }
    }
}
