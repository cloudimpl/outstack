/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nuwan
 */
public class MemEventRepositoryFactory implements EventRepositoryFactory {

    private final ResourceHelper helper;
    private final Map<Class<? extends RootEntity>,EventRepositoy<? extends RootEntity>> mapRepos = new ConcurrentHashMap<>();
    public MemEventRepositoryFactory(ResourceHelper helper) {
        this.helper = helper;
    }

    @Override
    public <T extends RootEntity> EventRepositoy<T> createOrGetRepository(Class<T> rootType) {
        return (EventRepositoy<T>) mapRepos.computeIfAbsent(rootType,type->new MemEventRepository<>((Class<T>)type,this.helper, null));
    }

}
