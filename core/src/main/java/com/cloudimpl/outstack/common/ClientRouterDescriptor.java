/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.outstack.common;


/**
 *
 * @author nuwansa
 */
public class ClientRouterDescriptor {
  private final RouterType routerType;
  private final String loadBalancer;

  public ClientRouterDescriptor(RouterType routerType, String loadBalancer) {
    this.routerType = routerType;
    this.loadBalancer = loadBalancer;
  }

  public RouterType getRouterType() {
    return routerType;
  }

  public String getLoadBalancer() {
    return loadBalancer;
  }
}
