/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import org.reactivestreams.Publisher;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class SpringService<T extends RootEntity>{
    private final ServiceProvider<T,CloudMessage> serviceProvider;
    public SpringService(EventRepositoy<T> eventRepository, ResourceHelper resourceHelper) {
        serviceProvider = new ServiceProvider<>(eventRepository, resourceHelper);
    }
    
    public Publisher apply(CloudMessage msg)
    {
        return serviceProvider.apply(msg.data());
    }
    
}
