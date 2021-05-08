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

/**
 *
 * @author nuwan
 */
public class ServiceDescriptorVersionManager {
    private final Map<String,ServiceDescriptorManager> mapCmd;
    private final Map<String,ServiceDescriptorManager> mapQuery ;

    public ServiceDescriptorVersionManager() {
        this.mapCmd = new ConcurrentHashMap<>();
        this.mapQuery = new ConcurrentHashMap<>();
    }
    
    protected void putCmd(SpringServiceDescriptor serviceDesc)
    {
        ServiceDescriptorManager man = mapCmd.computeIfAbsent(serviceDesc.getVersion(),n-> new ServiceDescriptorManager());
        man.putByPlural(serviceDesc);
    }
    
    protected void putQuery(SpringServiceDescriptor serviceDesc)
    {
        ServiceDescriptorManager man = mapQuery.computeIfAbsent(serviceDesc.getVersion(),n-> new ServiceDescriptorManager());
        man.putByPlural(serviceDesc);
    }
    
    public Optional<ServiceDescriptorManager> getVersionForCmd(String version)
    {
        return Optional.ofNullable(mapCmd.get(version));
    }
    
     public Optional<ServiceDescriptorManager> getVersionForQuery(String version)
    {
        return Optional.ofNullable(mapQuery.get(version));
    }
}
