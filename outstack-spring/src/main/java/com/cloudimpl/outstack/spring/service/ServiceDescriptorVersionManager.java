/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.Pair;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class ServiceDescriptorVersionManager {

    private final Map<String, ServiceDescriptorManager> mapCmd;
    private final Map<String, ServiceDescriptorManager> mapQuery;

    public ServiceDescriptorVersionManager() {
        this.mapCmd = new ConcurrentHashMap<>();
        this.mapQuery = new ConcurrentHashMap<>();
    }

    protected void putCmd(SpringServiceDescriptor serviceDesc) {
        ServiceDescriptorManager man = mapCmd.computeIfAbsent(serviceDesc.getVersion(), n -> new ServiceDescriptorManager());
        man.putByPlural(serviceDesc);
    }

    protected void putQuery(SpringServiceDescriptor serviceDesc) {
        ServiceDescriptorManager man = mapQuery.computeIfAbsent(serviceDesc.getVersion(), n -> new ServiceDescriptorManager());
        man.putByPlural(serviceDesc);
    }

    public Optional<ServiceDescriptorManager> getVersionForCmd(String version) {
        return Optional.ofNullable(mapCmd.get(version));
    }

    public Optional<ServiceDescriptorManager> getVersionForQuery(String version) {
        return Optional.ofNullable(mapQuery.get(version));
    }

    public Collection<Pair<String,ServiceDescriptorManager>> getCmdDescriptors() {
        return mapCmd.entrySet().stream().map(s->new Pair<>(s.getKey(),s.getValue())).collect(Collectors.toList());
    }

    public Collection<Pair<String,ServiceDescriptorManager>> getQueryDescriptors() {
        return mapQuery.entrySet().stream().map(s->new Pair<>(s.getKey(),s.getValue())).collect(Collectors.toList());
    }
}
