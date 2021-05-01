/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityEventHandler;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import org.reactivestreams.Publisher;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class SpringService<T extends RootEntity>{
    private final ServiceProvider<T,CloudMessage> serviceProvider;
    public SpringService(EventRepositoy<T> eventRepository, ResourceHelper resourceHelper) {
        serviceProvider = new ServiceProvider<>(Util.extractGenericParameter(this.getClass(), SpringService.class, 0),eventRepository, resourceHelper);
    }
    
    public void registerCommandHandler(Class<? extends EntityCommandHandler> commandHandlerType)
    {
        this.serviceProvider.registerCommandHandler(commandHandlerType);
    }
    
    public void registerEventHandler(Class<? extends EntityEventHandler> eventHandlerType)
    {
        this.serviceProvider.registerEventHandler(eventHandlerType);
    }
    
    public Publisher apply(CloudMessage msg)
    {
        return serviceProvider.apply(msg.data());
    }
    
}
