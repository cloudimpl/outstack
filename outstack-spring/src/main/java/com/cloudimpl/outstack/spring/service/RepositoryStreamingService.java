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
import com.cloudimpl.outstack.runtime.EntityIdHelper;
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
import com.cloudimpl.outstack.runtime.repo.RepoStreamingReq.ResourceInfo;
import com.cloudimpl.outstack.runtime.repo.StreamEvent;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
            //     log.info("repo stream event {}", e.getEvent());
            Entity entity = (Entity) e.getEvent();
            String rootType = entity.isRoot() ? entity.getClass().getName() : ChildEntity.class.cast(entity).rootType().getName();
            Optional<List<RepoStreamingReq.ResourceInfo>> listOptional = req.getResources(rootType);
            if (listOptional.isPresent()) {
                List<RepoStreamingReq.ResourceInfo> list = listOptional.get();
                for (RepoStreamingReq.ResourceInfo info : list) {
                    if (info.getChildType() != null) {
                        if (onChildEventListener(info, entity)) {
                            return true;
                        }
                    } else {
                        if (onRootEventListener(info, entity)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }).cast(StreamEvent.class);
        return Flux.merge(subscribeToRootResources(req), eventsFlux);
    }

    private boolean onRootEventListener(RepoStreamingReq.ResourceInfo info, Entity entity) {
        if (info.getTenantId() != null && info.getTenantId().equals("*")) {
            if (info.getEntityId().equals("*")) {
                return true;
            } else if (EntityIdHelper.isTechnicalId(info.getEntityId())) {
                return info.getEntityId().equals(entity.id());
            } else {
                return info.getEntityId().equals(entity.entityId());
            }
        } else if (info.getTenantId() == null && entity.getTenantId() == null) {
            if (info.getEntityId().equals("*")) {
                return true;
            } else if (EntityIdHelper.isTechnicalId(info.getEntityId())) {
                return info.getEntityId().equals(entity.id());
            } else {
                return info.getEntityId().equals(entity.entityId());
            }
        } else if (info.getTenantId() != null && entity.getTenantId() != null && info.getTenantId().equals(entity.getTenantId())) {
            if (info.getEntityId().equals("*")) {
                return true;
            } else if (EntityIdHelper.isTechnicalId(info.getEntityId())) {
                return info.getEntityId().equals(entity.id());
            } else {
                return info.getEntityId().equals(entity.entityId());
            }
        }
        return false;
    }

    private boolean onChildEventListener(RepoStreamingReq.ResourceInfo info, Entity entity) {
        if (!info.getChildType().equals("*") && !info.getChildType().equals(entity.getClass().getName())) {
            return false;
        }

        if (!info.getEntityId().equals("*")) {
            if (EntityIdHelper.isTechnicalId(info.getEntityId())) {
                if (!info.getEntityId().equals(entity.id())) {
                    return false;
                }
            } else {
                if (!info.getEntityId().equals(entity.entityId())) {
                    return false;
                }
            }
        }

        if (info.getTenantId() != null && info.getTenantId().equals("*")) {
            if (info.getChildId().equals("*")) {
                return true;
            } else if (EntityIdHelper.isTechnicalId(info.getChildId())) {
                return info.getChildId().equals(entity.id());
            } else {
                return info.getChildId().equals(entity.entityId());
            }
        } else if (info.getTenantId() == null && entity.getTenantId() == null) {
            if (info.getChildId().equals("*")) {
                return true;
            } else if (EntityIdHelper.isTechnicalId(info.getChildId())) {
                return info.getChildId().equals(entity.id());
            } else {
                return info.getChildId().equals(entity.entityId());
            }
        } else if (info.getTenantId() != null && entity.getTenantId() != null && info.getTenantId().equals(entity.getTenantId())) {
            if (info.getChildId().equals("*")) {
                return true;
            } else if (EntityIdHelper.isTechnicalId(info.getChildId())) {
                return info.getChildId().equals(entity.id());
            } else {
                return info.getChildId().equals(entity.entityId());
            }
        }
        return false;
    }

    private Flux<StreamEvent> subscribeToRootResources(RepoStreamingReq req) {
        return Flux.fromIterable(req.getInitialDownloadResources()).map(s -> getResources(s)).flatMapIterable(l -> l);
    }

    private Collection<StreamEvent> getResources(ResourceInfo resourceInfo) {
        if (resourceInfo.getChildType() == null) {
            return (Collection<StreamEvent>) getEventRepo(resourceInfo.getEntityType()).getAllByRootType(CloudUtil.classForName(resourceInfo.getEntityType()), resourceInfo.getTenantId(), Query.PagingRequest.EMPTY).getItems().stream().
                    map(r -> new StreamEvent(StreamEvent.Action.ADD, r)).collect(Collectors.toList());
        } else {
            return (Collection<StreamEvent>) getEventRepo(resourceInfo.getEntityType()).getAllChildByType(CloudUtil.classForName(resourceInfo.getEntityType()), resourceInfo.getEntityId(), CloudUtil.classForName(resourceInfo.getChildType()), resourceInfo.getTenantId(), Query.PagingRequest.EMPTY).getItems().stream().
                    map(r -> new StreamEvent(StreamEvent.Action.ADD, r)).collect(Collectors.toList());
        }
    }

    private EventRepositoy getEventRepo(String rootType) {
        return EventRepositoryFactory.getRepository(CloudUtil.classForName(rootType)).orElseThrow(() -> new RuntimeException("repository " + rootType + " not found"));
    }
}
