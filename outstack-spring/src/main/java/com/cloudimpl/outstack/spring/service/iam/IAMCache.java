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
package com.cloudimpl.outstack.spring.service.iam;

import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.common.RetryUtil;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.domain.Policy;
import com.cloudimpl.outstack.runtime.domain.PolicyRef;
import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import com.cloudimpl.outstack.runtime.domain.PolicyStatementRef;
import com.cloudimpl.outstack.runtime.domain.Role;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;
import com.cloudimpl.outstack.runtime.repo.RepoStreamingReq;
import com.cloudimpl.outstack.runtime.repo.StreamEvent;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.StreamClient;
import com.cloudimpl.outstack.spring.service.RestControllerService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Retry;

/**
 *
 * @author nuwan
 */
@Component
@Slf4j
public class IAMCache {

    @Autowired
    private Cluster cluster;

    @Autowired
    private TenantProvider tenantProvider;

    private StreamClient streamClient;

    private FluxMap<String, Entity> entityCache;

    @Value("${outstack.apiGateway.syncPolicies:false}")
    private boolean syncPolicies;

    @Value("${outstack.apiGateway.roleServiceName:#{null}}")
    private String roleServiceName;

    @Value("${outstack.apiGateway.roleDomainOwner:#{null}}")
    private String roleDomainOwner;

    @Value("${outstack.apiGateway.roleDomainContext:#{null}}")
    private String roleDomainContext;

    @PostConstruct
    private void init() {
        log.info("checking sync policies status ,{}", syncPolicies);
        if (!syncPolicies) {
            log.info("sync policies disabled");
            return;
        }
        this.streamClient = new StreamClient(cluster);
        this.entityCache = new FluxMap<>(Schedulers.newSingle("iamCache"));

        subscribeToPolicyRef();
        subscribeToPolicyStatementRef();
        syncRole();
        syncAllMicroServices();
    }

    private void syncAllMicroServices() {

        RestControllerService.domainContextsFlux.flux()
                .doOnNext(d -> log.info("subscribe to microservice {}/{}", d.getValue().getDomainOwner(), d.getValue().getDomainContext()))
                .doOnNext(d -> subscribeToMicroService(d.getValue().getDomainOwner(), d.getValue().getDomainContext()))
                .doOnError(err -> log.error("error subscribing to domain context", err))
                .subscribe();
    }

    private void syncRole() {
        log.info("sync roles . {}/{}/{}", roleDomainOwner, roleDomainContext, roleServiceName);
        if (roleServiceName != null) {
            streamClient.subscribeToMicroService("non tenant role sync ", roleDomainOwner, roleDomainContext,
                    new RepoStreamingReq(Arrays.asList(new RepoStreamingReq.ResourceInfo(Role.class.getName(), "*", null)), Arrays.asList(new RepoStreamingReq.ResourceInfo(Role.class.getName(), "*", null))))
                    .doOnNext(e -> updateCache(e))
                    .doOnError(err -> log.error("error syncing roles ", err))
                    .subscribe();

            tenantProvider.subscribeToTenants().flatMap(tid -> streamClient.subscribeToMicroService("tenant " + tid + " role sync", roleDomainOwner, roleDomainContext,
                    new RepoStreamingReq(Arrays.asList(new RepoStreamingReq.ResourceInfo(Role.class.getName(), "*", tid)), Arrays.asList(new RepoStreamingReq.ResourceInfo(Role.class.getName(), "*", tid)))))
                    .doOnNext(e -> updateCache(e))
                    .doOnError(err -> log.error("error syncing roles ", err))
                    .subscribe();

//            cluster.requestReply(null, roleDomainOwner + "/" + roleDomainContext + "/" + roleServiceName, QueryByIdRequest.builder().withQueryName("ListRole").withVersion("v1").withPagingReq(Query.PagingRequest.EMPTY).build())
//                    .flatMapIterable(rs -> ((ResultSet) rs).getItems(Role.class)).doOnNext(e -> putToCache((Role) e))
//                    .doOnError(err -> log.error("error on list roles.{}", err))
//                    .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60)))).subscribe();
//            tenantProvider.subscribeToTenants().flatMap(tid -> cluster.requestReply(null, roleDomainOwner + "/" + roleDomainContext + "/" + roleServiceName, QueryByIdRequest.builder().withQueryName("ListRole").withTenantId(tid).withVersion("v1").withPagingReq(Query.PagingRequest.EMPTY).build())
//                    .flatMapIterable(rs -> ((ResultSet) rs).getItems(Role.class)).doOnNext(e -> putToCache((Role) e))
//                    .doOnError(err -> log.error("error on list roles.{}", err))
//                    .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60))))
//            ).subscribe();
        }
    }

    private void subscribeToMicroService(String domainOwner, String domainContext) {
        subscribeToPolicyStatementUpdate(domainOwner, domainContext);
        subscribeToPolicyUpdate(domainOwner, domainContext);
    }

    private void subscribeToPolicyStatementUpdate(String domainOwner, String domainContext) {
        streamClient.subscribeToMicroService("policy statement sync", domainOwner, domainContext, new RepoStreamingReq(Arrays.asList(new RepoStreamingReq.ResourceInfo(PolicyStatement.class.getName(), "*", null)),
                Arrays.asList(new RepoStreamingReq.ResourceInfo(PolicyStatement.class.getName(), "*", null))))
                .doOnNext(e -> updateCache(e))
                .subscribe();
//        cluster.requestReply(null, domainOwner + "/" + domainContext + "/v1/PolicyStatementQueryService", QueryByIdRequest.builder().withQueryName("ListPolicyStatement").withVersion("v1").withPagingReq(Query.PagingRequest.EMPTY).build())
//                .flatMapIterable(rs -> ((ResultSet) rs).getItems(PolicyStatement.class)).doOnNext(e -> putToCache((PolicyStatement) e))
//                .doOnError(err -> log.error("error on list policy statements.{}", err))
//                .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60))))
//                .subscribe();
    }

    private void subscribeToPolicyUpdate(String domainOwner, String domainContext) {
        streamClient.subscribeToMicroService("policy sync", domainOwner, domainContext, new RepoStreamingReq(Arrays.asList(new RepoStreamingReq.ResourceInfo(Policy.class.getName(), "*", null)),
                Arrays.asList(new RepoStreamingReq.ResourceInfo(Policy.class.getName(), "*", null), new RepoStreamingReq.ResourceInfo(PolicyStatementRef.class.getName(), "*", null))))
                .doOnNext(e -> updateCache(e))
                .subscribe();
//        cluster.requestReply(null, domainOwner + "/" + domainContext + "/v1/PolicyQueryService", QueryByIdRequest.builder().withQueryName("ListPolicy").withVersion("v1").withPagingReq(Query.PagingRequest.EMPTY).build())
//                .flatMapIterable(rs -> ((ResultSet) rs).getItems(Policy.class)).doOnNext(e -> putToCache((Policy) e))
//                .doOnError(err -> log.error("error on list policy.{}", err))
//                .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60))))
//                .subscribe();
    }

    public <T> Optional<T> getEntity(String id) {
        return Optional.ofNullable((T) entityCache.get(id));
    }

    public Collection<Policy> listPolicy(String domainOwner, String domainContext) {
        return entityCache.values().stream().filter(e -> Policy.class.isInstance(e)).map(e -> Policy.class.cast(e))
                .filter(p -> p.getDomainOwner().equals(domainOwner) && p.getDomainContext().equals(domainContext))
                .collect(Collectors.toList());
    }

    public Collection<Policy> getPoliciesFromRole(Role role) {
        return entityCache.values().stream().filter(e -> PolicyRef.class.isInstance(e)).map(e -> PolicyRef.class.cast(e))
                .filter(pf -> pf.rootId().equals(role.id()))
                .filter(pf -> {
                    boolean exist = entityCache.get(EntityIdHelper.refIdToId(pf.getPolicyRef())) != null;
                    if (!exist) {
                        log.warn("policy {} not found for role {}, ignoring ....", EntityIdHelper.refIdToId(pf.getPolicyRef()), role.id());
                    }
                    return exist;
                })
                .map(pf -> (Policy) entityCache.get(EntityIdHelper.refIdToId(pf.getPolicyRef())))
                .collect(Collectors.toList());
    }

    public Collection<PolicyStatement> getStatementsFromPolicy(Policy policy) {
        return entityCache.values().stream().filter(e -> PolicyStatementRef.class.isInstance(e)).map(e -> PolicyStatementRef.class.cast(e))
                .filter(pf -> pf.rootId().equals(policy.id()))
                .filter(pf -> {
                    boolean exist = entityCache.get(EntityIdHelper.refIdToId(pf.entityId())) != null;
                    if (!exist) {
                        log.warn("policy statement {} not found for policy {}, ignoring ....", EntityIdHelper.refIdToId(pf.entityId()), policy.id());
                    }
                    return exist;
                })
                .map(pf -> (PolicyStatement) entityCache.get(EntityIdHelper.refIdToId(pf.entityId())))
                .collect(Collectors.toList());
    }

    private void putToCache(Entity entity) {
        Entity old = entityCache.get(entity.id());
        log.info("synced {} : {}", entity, old);
        if (old == null) {
            this.entityCache.put(entity.id(), entity).subscribe();
        } else if (old.getMeta().getLastSeq() < entity.getMeta().getLastSeq()) {
            this.entityCache.put(entity.id(), entity).subscribe();
        }
    }

    private void updateCache(StreamEvent event) {
        if (event.getAction() == StreamEvent.Action.REMOVE) {
            removeFromCache(((Entity) event.getEvent()).id());
        } else {
            putToCache((Entity) event.getEvent());
        }

    }

    private void removeFromCache(String id) {
        Entity e = entityCache.get(id);
        if (e != null) {
            log.info("entity {} removed from cache.", e);
        }
        entityCache.remove(id).subscribe();
    }

    private Mono syncPolicyReference(Role role) {
        return cluster.requestReply(null, roleDomainOwner + "/" + roleDomainContext + "/" + roleServiceName, QueryByIdRequest.builder().withQueryName("ListPolicyRef")
                .withRootId(role.id()).withVersion("v1").withTenantId(role.getTenantId()).withPagingReq(Query.PagingRequest.EMPTY).build())
                .flatMapIterable(rs -> ((ResultSet) rs).getItems(PolicyRef.class)).doOnNext(e -> putToCache((PolicyRef) e))
                .doOnError(err -> log.error("error on list policyRef.{}", err))
                .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60)))).then();
    }

    private void subscribeToPolicyRef() {
        entityCache.flux().filter(e -> Role.class.isInstance(e.getValue()))
                .filter(e -> e.getType() == FluxMap.Event.Type.ADD || e.getType() == FluxMap.Event.Type.UPDATE)
                .map(e -> e.getValue())
                .doOnNext(e -> log.info("starting to sync policy references for role {}:{}", e.entityId(), e.id()))
                .flatMap(r -> syncPolicyReference((Role) r))
                .doOnError(err -> log.error("error on sync  policyRef stream.{}", err))
                .subscribe();
    }

    private Mono syncPolicyStatementReference(Policy policy) {

        return cluster.requestReply(null, policy.getDomainOwner() + "/" + policy.getDomainContext() + "/v1/PolicyQueryService", QueryByIdRequest.builder().withQueryName("ListPolicyStatementRef")
                .withRootId(policy.id()).withVersion("v1").withPagingReq(Query.PagingRequest.EMPTY).build())
                .doOnNext(e -> log.info("sync policy statement references received for policy {} : {}", policy.id(),ResultSet.class.cast(e).getItems().size()))
                .flatMapIterable(rs -> ((ResultSet) rs).getItems(PolicyStatementRef.class)).doOnNext(e -> putToCache((PolicyStatementRef) e))
                .doOnError(err -> log.error("error on list PolicyStatmentRef.{}", err))
                .retryWhen(RetryUtil.wrap(Retry.any().exponentialBackoffWithJitter(Duration.ofSeconds(5), Duration.ofSeconds(60)))).then();
    }

    private void subscribeToPolicyStatementRef() {
        entityCache.flux()
                .filter(e -> Policy.class.isInstance(e.getValue()))
                .filter(e -> e.getType() == FluxMap.Event.Type.ADD || e.getType() == FluxMap.Event.Type.UPDATE)
                .map(e -> e.getValue())
                .doOnNext(e -> log.info("starting to sync policy statement references for policy {}:{}", e.entityId(), e.id()))
                .flatMap(r -> syncPolicyStatementReference((Policy) r))
                .doOnError(err -> log.error("error on sync  PolicyStatmentRef stream.{}", err))
                .subscribe();
    }

//    private void syncPolicyRef(Role r) {
//        cluster.requestReply(null, roleServiceName, log)
//    }
//
//    public  final class RoleCache {
//
//        private String id;
//        private Role role;
//        private final Map<String, PolicyStatement> policyStmt = new ConcurrentHashMap<>();
//
//        public RoleCache(String id,Role role) {
//            this.id = id;
//            this.role = role;
//        }
//
//        private void init()
//        {
//        }
//    }
}
