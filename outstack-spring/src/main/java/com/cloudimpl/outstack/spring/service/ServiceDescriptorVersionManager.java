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
    private final Map<String,ServiceDescriptorManager> map;

    public ServiceDescriptorVersionManager() {
        this.map = new ConcurrentHashMap<>();
    }
    
    protected void put(SpringServiceDescriptor serviceDesc)
    {
        ServiceDescriptorManager man = map.get(serviceDesc.getVersion());
        if(man == null)
        {
            man = new ServiceDescriptorManager();
            map.put(serviceDesc.getVersion(), man);
        }
        man.putByPlural(serviceDesc);
    }
    
    public Optional<ServiceDescriptorManager> getVersion(String version)
    {
        return Optional.ofNullable(map.get(version));
    }
}
