/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.outstack.routers;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.core.CloudRouter;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.Named;
import com.cloudimpl.outstack.core.RouterException;
import com.cloudimpl.outstack.coreImpl.CloudServiceRegistry;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwansa
 */
public class ServiceIdRouter implements CloudRouter {

  private final String topic;
  private final CloudServiceRegistry registry;

  @Inject
  public ServiceIdRouter(@Named("@topic") String topic, CloudServiceRegistry serviceRegistry) {
    this.topic = topic;
    this.registry = serviceRegistry;
  }

  @Override
  public Mono<CloudService> route(CloudMessage msg) {
    CloudService srv = this.registry.findService(msg.getKey());
    if (srv != null && srv.name().equals(topic))
      return Mono.just(srv);
    else
      return Mono.error(new RouterException("service not found to route for topic [" + topic + "]"));
  }

}
