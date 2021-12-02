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
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.workflow.Workflow;
import com.cloudimpl.outstack.workflow.domain.WorkflowCreateRequest;
import com.cloudimpl.outstack.workflow.domain.WorkflowEntity;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Component
public class WorkflowRepository {
    
    @Autowired
    private Cluster cluster;
    
    @Value("{outstack.domainOwner}")
    private String domainOwner;
    
    @Value("{outstack.domainContext}")
    private String domainContext;
    
    private final Map<String,Workflow> workFlows = new ConcurrentHashMap<>();
    
    
    public Mono<String> register(Workflow workflow)
    {
        JsonObject json = workflow.toJson();
        return cluster.requestReply(null,domainOwner+"/"+domainContext+"/"+RootEntity.getVersion(WorkflowEntity.class)+"/WorkflowService",WorkflowCreateRequest.builder().withContent(json.toString()).build())
                .cast(CommandResponse.class).map(cr->(String)cr.getValue());
    }
}
