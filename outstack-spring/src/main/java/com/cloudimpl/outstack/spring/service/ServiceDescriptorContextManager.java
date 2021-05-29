/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 *
 * @author nuwan
 */
@Component
public class ServiceDescriptorContextManager {
    private final Map<String,ServiceDescriptorVersionManager> appContexts;

    public ServiceDescriptorContextManager() {
        this.appContexts = new ConcurrentHashMap<>();
    }
    
    public void putCmdContext(String context,String version,SpringServiceDescriptor serviceDescriptor)
    {
        appContexts.computeIfAbsent(context, ctx->new ServiceDescriptorVersionManager()).putCmd(serviceDescriptor);
    }
    
    
    public void putQueryContext(String context,String version,SpringServiceDescriptor serviceDescriptor)
    {
        appContexts.computeIfAbsent(context, ctx->new ServiceDescriptorVersionManager()).putQuery(serviceDescriptor);
    }
    
    public  Optional<ServiceDescriptorManager> getCmdServiceDescriptorManager(String context,String version)
    {
        return Optional.ofNullable(appContexts.get(context)).flatMap(ctx->ctx.getVersionForCmd(version));
    }
    
    public  Optional<ServiceDescriptorManager> getQueryServiceDescriptorManager(String context,String version)
    {
        return Optional.ofNullable(appContexts.get(context)).flatMap(ctx->ctx.getVersionForQuery(version));
    }
}
