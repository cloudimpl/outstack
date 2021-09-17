/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwansa
 */
public class RoundRobinRouter implements CloudRouter {

  private Set<CloudService> services = new ConcurrentSkipListSet<>();
  private Iterator<CloudService> iterator;
  private final String topic;

  @Inject
  public RoundRobinRouter(@Named("@topic") String topic, ServiceRegistryReadOnly serviceRegistry) {
    this.topic = topic;
    serviceRegistry.services().filter(s->s.name().equals(topic)).forEach(s->services.add(s));
    serviceRegistry.flux("RoundRobinRouter:"+topic+":1").filter(e -> e.getType() == FluxMap.Event.Type.ADD)
        .map(e -> e.getValue())
        .filter(srv -> srv.name().equals(topic))
        .doOnNext(srv -> services.add(srv))
        .subscribe();
    serviceRegistry.flux("RoundRobinRouter:"+topic+":1").filter(e -> e.getType() == FluxMap.Event.Type.REMOVE)
        .map(e -> e.getValue())
        .filter(srv -> srv.name().equals(topic))
        .doOnNext(srv -> services.remove(srv))
        .subscribe();
    iterator = services.iterator();
  }

  @Override
  public Mono<CloudService> route(CloudMessage msg) {
    if (!iterator.hasNext())
      iterator = services.iterator();

    if (iterator.hasNext())
      return Mono.just(iterator.next());
    else
      return Mono.error(new RouterException("service [" + topic + "] not found"));

  }

}
