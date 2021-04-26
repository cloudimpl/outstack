/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.core;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;
import java.util.function.Function;

/**
 *
 * @author nuwan
 * @param <T>
 */
public  class MicroService<T extends RootEntity> extends ServiceProvider<T, CloudMessage>{
  
    @Inject
    public MicroService(EventRepositoy<T> eventRepository, ResourceHelper resourceHelper) {
        super(eventRepository, resourceHelper);
    }
  
    
}
