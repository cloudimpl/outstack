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
import com.cloudimpl.outstack.core.CloudRouter;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.Named;
import com.cloudimpl.outstack.core.RouterException;
import com.cloudimpl.outstack.core.ServiceRegistryReadOnly;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwansa
 */
public class LocalRouter implements CloudRouter {

    private CloudService service;
    private final String topic;

    @Inject
    public LocalRouter(@Named("@topic") String topic, ServiceRegistryReadOnly serviceRegistry) {
        this.topic = topic;
        serviceRegistry.localFlux().filter(e -> e.getType() == FluxMap.Event.Type.ADD || e.getType() == FluxMap.Event.Type.UPDATE)
                .map(e -> e.getValue()).filter(e -> e.name().equals(topic)).doOnNext(this::setService).subscribe();
        serviceRegistry.localFlux().filter(e -> e.getType() == FluxMap.Event.Type.REMOVE)
                .map(e -> e.getValue()).filter(e -> e.name().equals(topic)).doOnNext(e->setService(null)).subscribe();
    }

    @Override
    public Mono<CloudService> route(CloudMessage msg) {
        CloudService srv = this.service;
        if (srv != null) {
            return Mono.just(srv);
        } else {
            return Mono.error(new RouterException("service not found to route for topic [" + topic + "]"));
        }
    }

    
    private void setService(CloudService service)
    {
        this.service = service;
    }
}
