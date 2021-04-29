/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.core;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 * @param <T>
 */
@CloudFunction(name = "FirstService")
@Router(routerType = RouterType.ROUND_ROBIN)
public  class MicroService<T extends RootEntity> extends ServiceProvider<T, CloudMessage>{
  
    @Inject
    public MicroService(Class<T> rootType,EventRepositoryFactory eventRepositoryFactory, ResourceHelper resourceHelper) {
        super(eventRepositoryFactory.createRepository(rootType), resourceHelper);
    }
  
    
}
