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

import com.cloudimpl.outstack.coreImpl.CloudEngine;
import com.cloudimpl.outstack.runtime.CommandResponse;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.workflow.AbstractWork;
import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkStatus;
import com.cloudimpl.outstack.workflow.Workflow;
import com.cloudimpl.outstack.workflow.WorkflowEngine;
import com.cloudimpl.outstack.workflow.WorkflowException;
import com.cloudimpl.outstack.workflow.domain.WorkflowCreateRequest;
import com.cloudimpl.outstack.workflow.domain.WorkflowEntity;
import com.cloudimpl.outstack.workflow.domain.WorkflowUpdateRequest;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Component
@Slf4j
public class WorkflowRepository {

    @Autowired
    private Cluster cluster;

    @Value("{outstack.domainOwner}")
    private String domainOwner;

    @Value("{outstack.domainContext}")
    private String domainContext;

    private final Map<String, Workflow> workFlows = new ConcurrentHashMap<>();
    private final Map<String, WorkflowEngine> engines = new ConcurrentHashMap<>();

    public Mono<String> startWorkFlow(Workflow workflow) {
        JsonObject json = workflow.toJson();
        return cluster.requestReply(null, domainOwner + "/" + domainContext + "/" + RootEntity.getVersion(WorkflowEntity.class) + "/WorkflowService", WorkflowCreateRequest.builder().withContent(json.toString()).build())
                .cast(CommandResponse.class).map(cr -> (String) cr.getValue())
                .doOnNext(s -> workFlows.put(s, AbstractWork.fromJson(json).asWorkflow())).doOnNext(s->init(s));
    }

    private void init(String id) {
        Workflow workflow = workFlows.remove(id);
        if (workflow == null) {
            throw new WorkflowException("workflow {0} not found", id);
        }
        WorkflowEngine engine = new WorkflowEngine(id,this::updateState,cluster::requestReply);
        this.engines.put(id, engine);
        log.info("workflow {} started", id);
        engine.execute(workflow)
                .doOnTerminate(() -> removeEngine(id, false))
                .doOnCancel(() -> removeEngine(id, true))
                .doOnError(e -> log.error("workflow error for id :" + id, e))
                .subscribe();
    }

    public <T> Mono<T> execute(String id,String name,Function<WorkContext, T> handler){
        WorkflowEngine engine = engines.get(id);
        if(engine == null)
        {
            throw new WorkflowException("workflow engine {0} not found", id);
        }
        return engine.execute(name, handler);
    }
    
    private void removeEngine(String id, boolean cancel) {
        if (cancel) {
            log.info("workflow {} terminated . (canceled)", id);
        } else {
            log.info("workflow {} terminated .", id);
        }
        WorkflowEngine engine = engines.remove(id);
        if (engine != null) {
            //TODO cancel engine
        }
    }
    
    private Mono<WorkStatus> updateState(String workflowId,String id,WorkStatus status)
    {
         WorkflowUpdateRequest req = WorkflowUpdateRequest.builder()
                .withWorkflowId(workflowId)
                .withWorkId(id)
                .withWotkContext(status.getData())
                .withCommandName(UpdateWorkflow.class.getSimpleName()).withRootId(workflowId).withId(workflowId).build();
        return cluster.requestReply(domainOwner + "/" + domainContext + "/" + RootEntity.getVersion(WorkflowEntity.class) + "/WorkflowService", req).map(r->status);
    }
}
