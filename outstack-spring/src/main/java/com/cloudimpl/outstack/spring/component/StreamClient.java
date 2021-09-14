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
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.common.RetryUtil;
import com.cloudimpl.outstack.runtime.repo.RepoStreamingReq;
import com.cloudimpl.outstack.runtime.repo.StreamEvent;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Retry;

/**
 *
 * @author nuwan
 */
@Slf4j
public class StreamClient {

    private final Cluster cluster;

    public StreamClient(Cluster cluster) {
        this.cluster = cluster;
    }

    public Flux<StreamEvent> subscribeToMicroService(String name,String domainOwner,String domainContext, RepoStreamingReq req) {
        return this.cluster.getServiceRegistry().flux().filter(e -> e.getType() == FluxMap.Event.Type.ADD)
                .filter(e -> e.getValue().name().equals(domainOwner+"/"+domainContext+"/v1/PolicyService"))
                .map(e -> e.getValue())
                .flatMap(service -> {

                    return Mono.defer(() -> Mono.just(service))
                            .doOnNext(srv->log.info("initializing service {}-> {}/{} subscription",name,domainOwner,domainContext))
                            .flatMapMany(srv -> cluster.requestStream("RepositoryStreamingService", new CloudMessage(req, service.nodeId()))
                            .doOnError(thr->log.error("stream subscription error for service "+service.name()+" node id : "+service.nodeId(), thr))
                            .retryWhen(RetryUtil.wrap(Retry.onlyIf(ctx -> this.cluster.getServiceRegistry().findService(srv.id()) != null)
                                    .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofSeconds(60))))
                            .doOnError(thr->log.error("service {}:{} not found .terminating the stream",srv.name(),srv.id()))
                            .onErrorResume(thr -> Flux.empty())).publishOn(Schedulers.parallel());
                }).cast(StreamEvent.class);
    }
}
