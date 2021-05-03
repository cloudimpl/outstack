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
public class ServiceDescriptorManager {
    private final Map<String,SpringServiceDescriptor> map = new ConcurrentHashMap<>();
    
    protected void putByPlural(SpringServiceDescriptor serviceDescriptor)
    {
        this.map.put(serviceDescriptor.getPlural(), serviceDescriptor);
    }
    
    public Optional<SpringServiceDescriptor> getServiceDescriptorByPlural(String rootTypePlural)
    {
        return Optional.ofNullable(map.get(rootTypePlural));
    }
}
