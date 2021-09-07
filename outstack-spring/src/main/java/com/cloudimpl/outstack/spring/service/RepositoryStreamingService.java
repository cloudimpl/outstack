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
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.CloudUtil;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.runtime.EntityMetaDetailCache;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.common.StreamProcessor;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;
import com.cloudimpl.outstack.runtime.repo.RepoStreamingReq;
import com.cloudimpl.outstack.runtime.repo.StreamEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "RepositoryStreamingService")
@Router(routerType = RouterType.NODE_ID)
@Slf4j
public class RepositoryStreamingService implements Function<CloudMessage, Flux> {

    @Override
    public Flux<StreamEvent> apply(CloudMessage t) {
        RepoStreamingReq req = t.data();

        StreamProcessor<StreamEvent> eventStream = EventRepositoryFactory.<StreamEvent>getEventStream();
        Flux<StreamEvent> eventsFlux = eventStream.flux().filter(e -> {
            log.info("repo stream event {}", e);
            Entity entity = (Entity) e.getEvent();
            Optional<List<RepoStreamingReq.ResourceInfo>> listOptional = req.getResources(e.getEvent().getClass().getName());
            if (listOptional.isPresent()) {
                List<RepoStreamingReq.ResourceInfo> list = listOptional.get();
                for (RepoStreamingReq.ResourceInfo info : list) {
                    if (info.getTenantId() != null && info.getTenantId().equals("*")) {
                        if (info.getEntityId().equals("*")) {
                            return true;
                        } else if (info.getEntityId().equals(info.getEntityId())) {
                            return true;
                        }
                    }
                    else if (info.getTenantId() == null && entity.getTenantId() == null) {
                        if (info.getEntityId().equals("*")) {
                            return true;
                        } else if (info.getEntityId().equals(info.getEntityId())) {
                            return true;
                        }
                    } else if (info.getTenantId() != null && entity.getTenantId() != null && info.getTenantId().equals(entity.getTenantId())) {
                        if (info.getEntityId().equals("*")) {
                            return true;
                        } else if (info.getEntityId().equals(info.getEntityId())) {
                            return true;
                        }
                    }

                }
            }
            return false;
        }).cast(StreamEvent.class);
        return Flux.merge(subscribeToRootResources(req), eventsFlux);
    }

    private Flux<StreamEvent> subscribeToRootResources(RepoStreamingReq req) {
        return Flux.fromIterable(req.getInitialDownloadResources()).map(res -> getEventRepo(res.getEntityType()).getAllByRootType(CloudUtil.classForName(res.getEntityType()), res.getTenantId(), Query.PagingRequest.EMPTY))
                .flatMapIterable(rs -> rs.getItems()).map(r -> new StreamEvent(StreamEvent.Action.ADD, r));
    }

    private EventRepositoy getEventRepo(String rootType) {
        return EventRepositoryFactory.getRepository(CloudUtil.classForName(rootType)).orElseThrow(() -> new RuntimeException("repository " + rootType + " not found"));
    }
}
