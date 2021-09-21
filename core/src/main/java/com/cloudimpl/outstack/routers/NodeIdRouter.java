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
import com.cloudimpl.outstack.coreImpl.CloudServiceRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwansa
 */
public class NodeIdRouter implements CloudRouter {

    private final Map<String, CloudService> nodeIdTopics = new ConcurrentHashMap<>();
    private final String topic;

    @Inject
    public NodeIdRouter(@Named("@topic") String topic, ServiceRegistryReadOnly serviceRegistry) {
        this.topic = topic;
        serviceRegistry.services().filter(e -> e.name().equals(topic)).forEach(s -> nodeIdTopics.put(s.nodeId(), s));
        serviceRegistry.flux("NodeIdRouter:" + topic + ":1").filter(e -> e.getType() == FluxMap.Event.Type.ADD || e.getType() == FluxMap.Event.Type.UPDATE)
                .map(e -> e.getValue()).filter(e -> e.name().equals(topic))
                .doOnNext(e -> System.out.println(this + "added node id router " + e))
                .doOnNext(e -> nodeIdTopics.put(e.nodeId(), e)).subscribe();
        serviceRegistry.flux("NodeIdRouter:" + topic + ":2").filter(e -> e.getType() == FluxMap.Event.Type.REMOVE)
                .map(e -> e.getValue()).filter(e -> e.name().equals(topic))
                .doOnNext(e -> System.out.println(this + "removed node id router " + e))
                .doOnNext(e -> nodeIdTopics.remove(e.nodeId())).subscribe();
    }

    @Override
    public Mono<CloudService> route(CloudMessage msg) {
        return Mono.defer(() -> {
            CloudService srv = nodeIdTopics.get(msg.getKey());
            if (srv != null) {
                return Mono.just(srv);
            } else {
                System.out.println("testing" + nodeIdTopics.size() + " Key value " + msg.getKey());
                StringBuilder builder = new StringBuilder();
                builder.append("service not found to route for topic [" + topic + "]");

                nodeIdTopics.entrySet().forEach(s -> builder.append(s));
                return Mono.error(new RouterException(builder.toString()));
            }
        });
    }

}
