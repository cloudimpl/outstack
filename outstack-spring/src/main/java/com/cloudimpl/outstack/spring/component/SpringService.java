/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityEventHandler;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.HashSet;
import java.util.Set;
import org.reactivestreams.Publisher;
import com.cloudimpl.outstack.runtime.Handler;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class SpringService<T extends RootEntity> implements Function<CloudMessage, Publisher>{
    
    private static Set<Class<? extends Handler<?>>> HANDLERS = new HashSet<>();
    
    private final ServiceProvider<T,CloudMessage> serviceProvider;
    public SpringService(EventRepositoryFactory factory, ResourceHelper resourceHelper) {
        Class<T> root = Util.extractGenericParameter(this.getClass(), SpringService.class, 0);
        serviceProvider = new ServiceProvider<>(root,factory.createRepository(root));
        HANDLERS.stream()
                .filter(h->SpringService.filter(root, h))
                .filter(h->EntityCommandHandler.class.isAssignableFrom(h))
                .forEach(e->serviceProvider.registerCommandHandler((Class<? extends EntityCommandHandler>) e));
        HANDLERS.stream()
                .filter(h->SpringService.filter(root, h))
                .filter(h->EntityEventHandler.class.isAssignableFrom(h))
                .forEach(e->serviceProvider.registerEventHandler((Class<? extends EntityEventHandler>) e));
        
        HANDLERS.stream()
                .filter(h->SpringService.filter(root, h))
                .filter(h->EntityQueryHandler.class.isAssignableFrom(h))
                .forEach(e->serviceProvider.registerQueryHandler((Class<? extends EntityQueryHandler>) e));
    }
    
    public static void $(Class<? extends Handler<?>> handler)
    {
        HANDLERS.add(handler);
    }
    
    public Publisher apply(CloudMessage msg)
    {
        return serviceProvider.apply(msg.data());
    }
    
    public static boolean filter(Class<? extends RootEntity> rootType,Class<? extends Handler<?>> handlerType)
    {
        Class<? extends Entity> entityType = Util.extractGenericParameter(handlerType, handlerType.getSuperclass(), 0);
        Class<? extends Entity> root = RootEntity.isMyType(entityType)? entityType: Util.extractGenericParameter(entityType,ChildEntity.class, 0);
        return rootType == root;
    }
    
    public static Collection<Class<? extends Handler<?>>> handlers(Class<? extends RootEntity> rootType)
    {
        return HANDLERS.stream().filter(h->filter(rootType, h)).collect(Collectors.toList());
    }
    
    public static boolean isCommandHandler(Class<? extends Handler<?>> handlerType)
    {
        return EntityCommandHandler.class.isAssignableFrom(handlerType);
    }
    
    public static boolean isEventHandler(Class<? extends Handler<?>> eventType)
    {
        return EntityEventHandler.class.isAssignableFrom(eventType);
    }
}
