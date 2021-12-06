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

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.common.RetryUtil;
import com.cloudimpl.outstack.coreImpl.CloudEngine;
import com.cloudimpl.outstack.runtime.CommandResponse;
import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.service.iam.TenantProvider;
import com.cloudimpl.outstack.workflow.AbstractWork;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkStatus;
import com.cloudimpl.outstack.workflow.Workflow;
import com.cloudimpl.outstack.workflow.WorkflowEngine;
import com.cloudimpl.outstack.workflow.WorkflowException;
import com.cloudimpl.outstack.workflow.domain.WorkflowCompleteRequest;
import com.cloudimpl.outstack.workflow.domain.WorkflowCreateRequest;
import com.cloudimpl.outstack.workflow.domain.WorkflowEntity;
import com.cloudimpl.outstack.workflow.domain.WorkflowUpdateRequest;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

/**
 *
 * @author nuwan
 */
@Component
@Slf4j
public class WorkflowRepository {

    @Autowired
    private Cluster cluster;

    @Autowired
    private TenantProvider tenantProvider;
    @Value("${outstack.domainOwner}")
    private String domainOwner;

    @Value("${outstack.domainContext}")
    private String domainContext;

    private final Map<String, Workflow> workFlows = new ConcurrentHashMap<>();
    private final Map<String, WorkflowEngine> engines = new ConcurrentHashMap<>();
    private final Map<String, WorkflowEntity> entityCache = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        log.info("initializing WorkflowRepository");
        tenantProvider.subscribeToTenants("WorkflowRepository").flatMap(tenant -> loadTenantWorkflows(tenant)).subscribe();
    }

    public Mono<String> startWorkFlow(String tenantId, Workflow workflow) {
        JsonObject json = workflow.toJson();
        return cluster.requestReply(null, domainOwner + "/" + domainContext + "/" + RootEntity.getVersion(WorkflowEntity.class) + "/WorkflowService", WorkflowCreateRequest
                .builder()
                .withContent(json.toString())
                .withCommandName(CreateWorkflow.class.getSimpleName())
                .withTenantId(tenantId)
                .withVersion("v1")
                .build())
                .cast(WorkflowEntity.class)
                .doOnNext(s -> entityCache.put(s.entityId(), s))
                .doOnNext(s -> workFlows.put(s.entityId(), AbstractWork.fromJson(json).asWorkflow())).doOnNext(s -> init(s.entityId(), tenantId)).map(s -> s.entityId());
    }

    private void init(String id, String tenantId) {
        Workflow workflow = workFlows.remove(id);
        if (workflow == null) {
            throw new WorkflowException("workflow {0} not found", id);
        }
        WorkflowEngine engine = new WorkflowEngine(id, tenantId, this::updateState, cluster::requestReply, this::loadWorkStatus);
        WorkflowEngine old = this.engines.putIfAbsent(id, engine);
        if(old != null)
        {
             log.warn("workflow {} already exist , ignored", id);
             return;
        }
        log.info("workflow {} started", id);
        engine.execute(workflow)
                .doOnTerminate(() -> removeEngine(id, false))
                .doOnCancel(() -> removeEngine(id, true))
                .doOnError(e -> log.error("workflow error for id :" + id, e))
                .subscribe();
    }

    private Mono loadTenantWorkflows(String tenantId) {
        log.info("loading workflow for tenantId {}", tenantId);
        return cluster.requestReply(null, domainOwner + "/" + domainContext + "/" + RootEntity.getVersion(WorkflowEntity.class) + "/WorkflowQueryService", QueryByIdRequest.builder().withQueryName("ListWorkflowEntity").withPagingReq(Query.PagingRequest.EMPTY).withTenantId(tenantId).withVersion("v1").build())
                .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(3), Duration.ofSeconds(60))))
                .cast(ResultSet.class)
                .flatMapIterable(rs -> rs.getItems(WorkflowEntity.class))
                .filter(e -> WorkflowEntity.class.cast(e).getStatus() == Work.Status.PENDING)
                .doOnNext(e -> log.info("loading workflow {}", WorkflowEntity.class.cast(e).entityId()))
                .doOnNext(s -> entityCache.put(WorkflowEntity.class.cast(s).entityId(), WorkflowEntity.class.cast(s)))
                .doOnNext(s->autoStartWorkFlow(WorkflowEntity.class.cast(s)))
                .doOnError((err)->log.error("Error loading workflows", err))
                .then();
    }

    private void autoStartWorkFlow(WorkflowEntity e) {
        Workflow workFlow = AbstractWork.fromJson(GsonCodec.toJsonObject(e.getContent())).asWorkflow();
        workFlows.put(e.entityId(), workFlow);
        init(e.entityId(),e.getTenantId());
    }

    public <T> Mono<T> executeAsync(String id, String name, Function<WorkContext, Mono<Object>> handler) {
        WorkflowEngine engine = engines.get(id);
        if (engine == null) {
            throw new WorkflowException("workflow engine {0} not found", id);
        }
        return engine.executeAsync(name, handler);
    }

    public <T> T execute(String id, String name, Function<WorkContext, Object> handler) {
        WorkflowEngine engine = engines.get(id);
        if (engine == null) {
            throw new WorkflowException("workflow engine {0} not found", id);
        }
        return engine.execute(name, handler);
    }

    public <T> Mono<T> executeAsyncNext(String id, Function<WorkContext, Mono<Object>> handler) {
        WorkflowEngine engine = engines.get(id);
        if (engine == null) {
            throw new WorkflowException("workflow engine {0} not found", id);
        }
        return engine.executeAsyncNext(handler);
    }

    public <T> T executeNext(String id, Function<WorkContext, Object> handler) {
        WorkflowEngine engine = engines.get(id);
        if (engine == null) {
            throw new WorkflowException("workflow engine {0} not found", id);
        }
        return engine.executeNext(handler);
    }

    private void removeEngine(String id, boolean cancel) {
        if (cancel) {
            log.info("workflow {} terminated . (canceled)", id);
        } else {
            log.info("workflow {} terminated .", id);
        }
        WorkflowEngine engine = engines.remove(id);
        this.entityCache.remove(id);
        if (engine != null) {
            //TODO cancel engine
        }
    }

    private Mono<WorkStatus> updateState(String workflowId, String id, String tenantId, WorkStatus status) {
        Command req;
        if (id.equals(workflowId)) {
            req = WorkflowCompleteRequest.builder()
                    .withWorkflowId(workflowId)
                    .withStatus(status.getStatus())
                    .withTenantId(tenantId)
                    .withVersion("v1")
                    .withCommandName(CompleteWorkflow.class.getSimpleName()).withRootId(workflowId).withId(workflowId).build();
        } else {
            req = WorkflowUpdateRequest.builder()
                    .withWorkContext(status.getData())
                    .withWorkflowId(workflowId)
                    .withWorkId(id)
                    .withTenantId(tenantId)
                    .withVersion("v1")
                    .withCommandName(UpdateWorkflow.class.getSimpleName()).withRootId(workflowId).withId(workflowId).build();
        }

        return cluster.requestReply(domainOwner + "/" + domainContext + "/" + RootEntity.getVersion(WorkflowEntity.class) + "/WorkflowService", req)
                .cast(WorkflowEntity.class)
                .doOnNext(s -> entityCache.put(s.entityId(), s))
                .map(r -> status);
    }

    private Optional<WorkStatus> loadWorkStatus(String workflowId, String workId) {
        WorkflowEntity e = entityCache.get(workflowId);
        return e.getStatus(workId);
    }
}
