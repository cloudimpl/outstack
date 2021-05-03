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
import java.util.HashSet;
import java.util.Set;
import org.reactivestreams.Publisher;
import com.cloudimpl.outstack.runtime.Handler;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class SpringService<T extends RootEntity>{
    
    private static Set<Class<? extends Handler>> HANDLERS = new HashSet<>();
    
    private final ServiceProvider<T,CloudMessage> serviceProvider;
    public SpringService(EventRepositoy<T> eventRepository, ResourceHelper resourceHelper) {
        serviceProvider = new ServiceProvider<>(Util.extractGenericParameter(this.getClass(), SpringService.class, 0),eventRepository, resourceHelper);
        HANDLERS.stream().filter(h->EntityCommandHandler.class.isAssignableFrom(h)).forEach(e->serviceProvider.registerCommandHandler((Class<? extends EntityCommandHandler>) e));
        HANDLERS.stream().filter(h->EntityEventHandler.class.isAssignableFrom(h)).forEach(e->serviceProvider.registerEventHandler((Class<? extends EntityEventHandler>) e));
    }
    
    public static void $(Class<? extends Handler> handler)
    {
        HANDLERS.add(handler);
    }
    
    public Publisher apply(CloudMessage msg)
    {
        return serviceProvider.apply(msg.data());
    }
}
