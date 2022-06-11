/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.routers;


import java.util.Collections;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.core.CloudRouter;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.Named;
import com.cloudimpl.outstack.core.RouterException;
import com.cloudimpl.outstack.core.ServiceRegistryReadOnly;
import org.ishugaliy.allgood.consistent.hash.HashRing;
import org.ishugaliy.allgood.consistent.hash.node.SimpleNode;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class ConsistentHashRouter implements CloudRouter {

    private final ServiceRegistryReadOnly serviceRegistry;
    private HashRing<SimpleNode> ring;
    private String topic;


    public ConsistentHashRouter(@Named("@topic") String topic, ServiceRegistryReadOnly serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.topic = topic;
        ring = HashRing.<SimpleNode>newBuilder().partitionRate(64).nodes(Collections.EMPTY_LIST).build();
        serviceRegistry.flux("ConsistentHashRouter:"+topic)
                .filter(e -> e.getType() == FluxMap.Event.Type.ADD)
                .map(e->e.getValue())
                .filter(c -> c.name().equals(topic))
                .doOnNext(s -> ring.add(SimpleNode.of(s.id()))).subscribe();

        serviceRegistry.flux("ConsistentHashRouter:"+topic)
                .filter(e -> e.getType() == FluxMap.Event.Type.REMOVE)
                .map(e->e.getValue())
                .filter(c -> c.name().equals(topic))
                .doOnNext(s ->  ring.remove(SimpleNode.of(s.id()))).subscribe();
    }

    @Override
    public Mono<CloudService> route(CloudMessage msg) {
        if (msg.getKey() == null) {
            return Mono.error(new RouterException("service route key not found"));
        }
        return Mono.justOrEmpty(ring.locate(msg.getKey()))
                .map(node -> this.serviceRegistry.findService(node.getKey())).switchIfEmpty(Mono.defer(()->Mono.error(()->new RouterException("service [".concat(topic).concat("] not found ")))));
    }
}