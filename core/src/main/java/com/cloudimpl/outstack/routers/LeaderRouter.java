/*
 * Copyright 2020 nuwansa.
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
package com.cloudimpl.outstack.routers;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.common.RetryUtil;
import com.cloudimpl.outstack.core.CloudException;
import com.cloudimpl.outstack.core.CloudRouter;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.Named;
import com.cloudimpl.outstack.core.RouterException;
import com.cloudimpl.outstack.core.ServiceRegistryReadOnly;
import com.cloudimpl.outstack.core.logger.ILogger;
import com.cloudimpl.outstack.coreImpl.CloudServiceRegistry;
import com.cloudimpl.outstack.le.LeaderElectionManager;
import com.cloudimpl.outstack.le.LeaderInfoRequest;
import com.cloudimpl.outstack.le.LeaderInfoResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;


/**
 *
 * @author nuwansa
 */
public class LeaderRouter implements CloudRouter {

    private final List<String> leaderServices = new CopyOnWriteArrayList<>();
    private volatile String leaderId = null;
    private final ILogger logger;
    private final ServiceRegistryReadOnly registry;
    private CloudService serviceInstance;
    private int nextPossibleId = 0;
    @Inject
    public LeaderRouter(@Named("@topic") String topic, @Named("RSHnd") BiFunction<String, CloudMessage, Flux<LeaderInfoResponse>> rsHnd,
            ILogger logger,
            ServiceRegistryReadOnly serviceRegistry, LeaderElectionManager leaderManager) {
        this.registry = serviceRegistry;
        this.logger = logger.createSubLogger("LeaderRouter",topic);
        serviceRegistry.flux().filter(e -> e.getType() == FluxMap.Event.Type.ADD).map(e -> e.getValue())
                .filter(c -> c.name().equals(topic))
                .doOnNext(cs -> update(cs.nodeId(), false)).subscribe();
        serviceRegistry.flux().filter(e -> e.getType() == FluxMap.Event.Type.REMOVE).map(e -> e.getValue())
                .filter(c -> c.name().equals(topic))
                .doOnNext(cs -> update(cs.nodeId(), true)).subscribe();
        Mono.fromSupplier(() -> getNodeId()).flatMapMany(nodeId -> rsHnd.apply("LeaderInfoService",
                CloudMessage.builder().withData(new LeaderInfoRequest(topic)).withKey(nodeId).build()))
                .doOnNext(resp->setLeaderId(resp.getLeaderId()))
                .doOnError(thr->logger.exception(thr, "error on finding leader"))
                .retryWhen(RetryUtil.wrap(Retry
                        .any()
                        .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofSeconds(20)))
                )
                .subscribe();
    }

    @Override
    public Mono<CloudService> route(CloudMessage msg) {
        if(leaderId == null)
            return Mono.error(new RouterException("leader not found"));
        return Mono.just(getService());
    }

    private synchronized void update(String nodeId, boolean remove) {
        logger.info("updating nodeId {0} removed : {1}", nodeId,remove);
        if (remove) {
            leaderServices.remove(nodeId);
        } else {
            leaderServices.add(nodeId);
        }
    }

    private CloudService getService()
    {
        if(serviceInstance == null)
            serviceInstance = this.registry.findService(leaderId);
        return serviceInstance;
    }
    private synchronized String getNodeId() {
        if(nextPossibleId >= leaderServices.size())
            nextPossibleId = 0;
        if (!leaderServices.isEmpty()) {
            
            return leaderServices.get(nextPossibleId++);
        }
        throw new CloudException("nodeId not found");
    }
    
    private void setLeaderId(String leaderId)
    {
        this.leaderId = leaderId;
        this.serviceInstance = null;
        logger.info("leader id updated : {0}", leaderId);
    }
}
