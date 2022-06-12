package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.spring.component.Cluster;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ReactiveServiceProxyHandler implements InvocationHandler {
    private Cluster cluster;
    private Class<? extends IReactiveService> serviceType;
    public ReactiveServiceProxyHandler(Class<? extends IReactiveService> serviceType,Cluster cluster)
    {
        this.serviceType = serviceType;
        this.cluster = cluster;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getReturnType() == Flux.class)
        {
            return cluster.requestStreamEx(serviceType.getName(),method.getName(),(String)args[0],args[1]);
        }
        else {
            return cluster.requestReplyEx(serviceType.getName(),method.getName(),(String)args[0],args[1]);
        }
    }
}
