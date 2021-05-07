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

/**
 *
 * @author nuwan
 */
public class MemEventRepositoryFactory implements EventRepositoryFactory {

    private final ResourceHelper helper;

    public MemEventRepositoryFactory(ResourceHelper helper) {
        this.helper = helper;
    }

    @Override
    public <T extends RootEntity> EventRepositoy<T> createRepository(Class<T> rootType) {
        return new MemEventRepository<>(rootType,this.helper, null);
    }

}
